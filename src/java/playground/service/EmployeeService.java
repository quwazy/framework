package playground.service;

import framework.annotations.components.Autowired;
import framework.annotations.components.Service;
import playground.components.AgeCheckComponent;

@Service
public class EmployeeService {
    private String name;
    private int age = 31;
    @Autowired
    private AgeCheckComponent ageCheckComponent;

    public void sayHello() {
        this.name = "Janko";
        System.out.println("Hello " + name + " " + age);
        if (ageCheckComponent.isAgeOddNumber(age)){
            System.out.println("Odd number");
        }
    }
}
