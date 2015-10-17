package ru.yandex.qatools.allure.testng;

import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.AllureEx;

public class TestNGListener  extends AllureTestListener{

    public TestNGListener() {
        System.out.println("TestAspect's TestNGListener... STARTED\n\n");
        super.setLifecycle(AllureEx.get());
    }

    @Override
    public Allure getLifecycle() {
        return AllureEx.get();
    }
}