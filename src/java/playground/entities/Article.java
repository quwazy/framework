package playground.entities;

import framework.annotations.databases.Entity;
import framework.annotations.databases.Id;

@Entity(tableName = "articles")
public class Article {
    @Id
    private Long id;
}
