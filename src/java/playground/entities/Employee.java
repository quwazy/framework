package playground.entities;

import framework.annotations.databases.Entity;
import framework.annotations.databases.Id;

@Entity(tableName = "employees")
public class Employee {
    @Id
    private Long id;
    private String name;
    private String email;

    public Employee(String name, String email) {
        this.name = name;
        this.email = email;
    }

}
