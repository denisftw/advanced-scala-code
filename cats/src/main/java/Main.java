import java.util.Arrays;
import java.util.List;

class Main {

    static void print(List list) {
        list.forEach(el -> System.out.println(el));
    }

    public static void main(String[] args) {
        List<Integer> ints = Arrays.asList(1,2,3);
        print(ints);
    }
}
