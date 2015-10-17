package com.testaspect;

import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.AllureEx;
import ru.yandex.qatools.allure.junit.AllureRunListener;

public class JUnitListener extends AllureRunListener {

    public JUnitListener(){
        System.out.println("TestAspect JUniteListener... STARTED\n\n");
        super.setLifecycle(AllureEx.get());
    }

    @Override
    public Allure getLifecycle() {
        return AllureEx.get();
    }
}