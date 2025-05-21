package playground.repositories;

import framework.annotations.components.Repository;
import framework.interfaces.FrameworkRepository;
import playground.entities.Employee;

@Repository(entity = Employee.class)
public interface EmployeeRepository extends FrameworkRepository<Employee> {
}
