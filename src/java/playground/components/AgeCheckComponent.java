package playground.components;

import framework.annotations.components.Component;

@Component
public class AgeCheckComponent {
    public boolean isAgeOddNumber(int age) {
        return age % 2 != 0;
    }
}
