package playground.controllers;

import framework.annotations.components.Controller;
import framework.annotations.methodes.Post;
import playground.entities.Employee;
import playground.repositories.EmployeeRepository;

@Controller(path = "/employees")
public class EmployeeController {
    private EmployeeRepository employeeRepository;

    @Post(path = "/add")
    public void addEmployee(Employee employee){
        System.out.println("Prvo ovo: " + employee.toString());
        employeeRepository.add(employee);
    }
}
