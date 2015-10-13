package ru.yandex.qatools.allure.testng;

import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.Spider;
import ru.yandex.qatools.allure.junit.AllureRunListener;
import ru.yandex.qatools.allure.testng.AllureTestListener;

public class TestNGListener extends AllureTestListener {

    public TestNGListener(){
        System.out.println("@TestAspect TestNG...\n\n\n\n\n");
        super.setLifecycle(Spider.get());
    }

    @Override
    public Allure getLifecycle() {
        return Spider.get();
    }
}
