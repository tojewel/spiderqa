package com.spiderqa.agent.junit;

import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.Spider;
import ru.yandex.qatools.allure.junit.AllureRunListener;

public class SpiderQaJUnitListener extends AllureRunListener {

    public SpiderQaJUnitListener(){
        System.out.println("SPIDER******************************LOMBOK\n\n\n\n\n");
        super.setLifecycle(Spider.get());
    }

    @Override
    public Allure getLifecycle() {
        return Spider.get();
    }
}
