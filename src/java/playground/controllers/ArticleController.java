package playground.controllers;

import framework.annotations.components.Autowired;
import framework.annotations.components.Controller;
import framework.annotations.methodes.Get;
import framework.annotations.methodes.Post;
import framework.http.JsonResponse;
import framework.http.Response;
import playground.entities.Article;
import playground.entities.Employee;
import playground.repositories.ArticleRepository;

@Controller( path = "/articles")
public class ArticleController {
    private ArticleRepository articleRepository;

    @Get(path = "/all")
    public Response getAllArticles(){
        return new JsonResponse(articleRepository.getAll());
    }

    @Post(path = "")
    public void addArticle(Article article){
        articleRepository.add(article);
    }
}
