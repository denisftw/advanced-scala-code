import java.nio.file.Paths

import fs2.{Stream, Task, text}
import fs2.io.file._

import io.circe.numbers.BiggerDecimal

/**
  * Created by denis on 8/12/16.
  */
object CirceMain {





  def main(args: Array[String]) {

    case class Person(firstName: String, lastName: String, age: Int)
    val person = Person("Joe", "Black", 42)

    // Manual
    {
      import io.circe.Encoder
      import io.circe.syntax._
      implicit val personEnc: Encoder[Person] = Encoder.forProduct3(
        "firstName", "lastName", "age"){ src =>  (src.firstName, src.lastName, src.age) }
      println(person.asJson)
    }

    // Semi-automatic
    {
      import io.circe.syntax._
      import io.circe.generic.semiauto._
      implicit val personEnc = deriveEncoder[Person]
      println(person.asJson)
    }

    // Automatic
    {
      import io.circe.syntax._
      import io.circe.generic.auto._
      println(person.asJson)
    }

    val jsonStr = """{ "firstName" : "Joe", "lastName" : "Black", "age" : 42 }"""

    // Manual decoder
    {
      {
        import io.circe.Decoder
        import io.circe.jawn._
        implicit val personDecoder: Decoder[Person] = Decoder.forProduct3(
          "firstName", "lastName", "age")(Person.apply)
        val person = decode[Person](jsonStr)
        println(person)
      }

      {
        import io.circe.Decoder
        import io.circe.jawn._
        implicit val personDecoder = for {
          firstName <- Decoder.instance(_.get[String]("firstName"))
          lastName <- Decoder.instance(_.get[String]("lastName"))
          age <- Decoder.instance(_.get[Int]("age"))
        } yield Person(firstName, lastName, age)
        val person = decode[Person](jsonStr)
        println(person)
      }

      {
        import io.circe.Decoder
        import io.circe.jawn._
        import cats.syntax.cartesian._
        val firstNameD = Decoder.instance(_.get[String]("firstName"))
        val lastNameD = Decoder.instance(_.get[String]("lastName"))
        val ageD = Decoder.instance(_.get[Int]("age"))
        implicit val personDecoder = (firstNameD |@| lastNameD |@| ageD).map(Person.apply)
        val person = decode[Person](jsonStr)
        println(person)
      }
    }

    // Semi-automatic decoder
    {
      import io.circe.generic.semiauto._
      import io.circe.jawn._
      implicit val personDec = deriveDecoder[Person]
      val person = decode[Person](jsonStr)
      println(person)
    }

    // Automatic
    {
      import io.circe.jawn._
      import io.circe.generic.auto._
      val person = decode[Person](jsonStr)
      println(person)
    }

    // Parsing numbers
    {
      val jsonStr =
        """{ "firstName" : "Joe", "lastName" : "Black", "age" : 42,
          |"address": { "street": "Market st.", "city": "Sydney",
          |"postal": 2000, "state": "NSW" },
          |"departments": ["dev", "hr", "qa"] }""".stripMargin

      import io.circe.jawn._
      import io.circe.syntax._
      val result = parse(jsonStr)
      // result: Xor[ParsingFailure, Json]

      import io.circe.Json
      val json = result.getOrElse(Json.Null)
      val cursor = json.hcursor

      cursor.downField("departments").downArray.right.withFocus(_.withString(_.toUpperCase.asJson))

      val modified = result.map { json =>
        json.hcursor.downField("age").withFocus(_.withNumber { currentAge =>
          val age = currentAge.truncateToInt
          val newValue = age + 1
          newValue.asJson
        }).top
      }
      modified.fold(
        fail => println(fail.message),
        res => println(res)
      )
    }

    case class Company(name: String, permalink: String, homepage_url: String)

    // Reading a large file
    {
      import io.circe.jawn._
      import io.circe.generic.auto._
      val company = decode[Company](jsonStr)

      val filePath = Paths.get("companies.json")
      val byteStr = readAll[Task](filePath, 1024)
      val lineStr = byteStr.through(text.utf8Decode).through(text.lines)
      val resultT = lineStr.map { line =>
        decode[Company](line)
      }.filter(_.isRight).map { company =>
        company.foreach(println)
        company
      }.take(5).runLog

      val diff = System.currentTimeMillis()
      resultT.unsafeRun()
      println(s"Elapsed: ${System.currentTimeMillis() - diff}")
    }

  }
}
