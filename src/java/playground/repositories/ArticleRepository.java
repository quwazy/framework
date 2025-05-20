package playground.repositories;

import framework.annotations.components.Repository;
import framework.interfaces.FrameworkRepository;
import playground.entities.Article;

@Repository(entity = Article.class)
public interface ArticleRepository extends FrameworkRepository<Article>{
}
