package playground.controllers;

import framework.annotations.components.Controller;
import framework.annotations.methodes.Delete;
import framework.annotations.methodes.Get;
import framework.annotations.methodes.Post;
import framework.http.JsonResponse;
import framework.http.Response;
import playground.entities.Employee;
import playground.repositories.EmployeeRepository;
import playground.service.EmployeeService;

import java.util.List;

@Controller(path = "/employees")
public class EmployeeController {
    private EmployeeRepository employeeRepository;
    private EmployeeService employeeService;

    @Post(path = "/add")
    public void addEmployee(Employee employee){
        employee.setName(employee.getName().toUpperCase());
        employeeRepository.add(employee);
        employeeService.sayHello();
    }

    @Get(path = "/getAllEmployees")
    public Response getAllEmployees(){
        List<Employee> employeeList = employeeRepository.getAll();
        return new JsonResponse(employeeList);
    }

    @Get(path = "/getOneEmployee")
    public Response getOneEmployee(Long id){
        Employee employee = employeeRepository.get(id);
        return new JsonResponse(employee);
    }

    @Delete(path = "/deleteEmployee")
    public void deleteEmployee(Long id){
        employeeRepository.delete(id);
    }
}
