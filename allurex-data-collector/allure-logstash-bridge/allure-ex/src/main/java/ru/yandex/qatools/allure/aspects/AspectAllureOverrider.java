package ru.yandex.qatools.allure.aspects;

import ru.yandex.qatools.allure.Allure;

public class AspectAllureOverrider {
    public static void override(Allure allure) {
        AllureStepsAspects.setAllure(allure);
        AllureAttachAspects.setAllure(allure);
    }
}