package com.spiderqa.agent.junit;

import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.Spider;
import ru.yandex.qatools.allure.junit.AllureRunListener;

public class JUnitListener extends AllureRunListener {

    public JUnitListener(){
        System.out.println("TestAspectListener... STARTED\n\n");
        super.setLifecycle(Spider.get());
    }

    @Override
    public Allure getLifecycle() {
        return Spider.get();
    }
}
