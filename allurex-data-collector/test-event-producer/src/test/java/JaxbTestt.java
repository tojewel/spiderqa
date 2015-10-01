import com.spiderqa.rest.Serializer;
import ru.yandex.qatools.allure.model.Execution;
import ru.yandex.qatools.allure.model.TestCase;
import ru.yandex.qatools.allure.model.TestCaseResult;

public class JaxbTestt {
    public static void main(String[] s) {
        TestCase testCase = new TestCase(new Execution(), new TestCaseResult());
        System.out.println(new Serializer().toJson(testCase));
    }
}
