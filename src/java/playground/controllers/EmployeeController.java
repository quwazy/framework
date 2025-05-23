package playground.controllers;

import framework.annotations.components.Controller;
import framework.annotations.methodes.Get;
import framework.annotations.methodes.Post;
import framework.server.http.JsonResponse;
import framework.server.http.Response;
import playground.entities.Employee;
import playground.repositories.EmployeeRepository;

@Controller(path = "/employees")
public class EmployeeController {
    private EmployeeRepository employeeRepository;

    @Post(path = "/add")
    public void addEmployee(Employee employee){
        employeeRepository.add(employee);
    }

    @Get(path = "/getAllEmployees")
    public Response getAllEmployees(){
        return new JsonResponse(employeeRepository.getAll());
    }
}
