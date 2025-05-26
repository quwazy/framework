package playground.controllers;

import framework.annotations.components.Controller;
import framework.annotations.methodes.Get;
import framework.annotations.methodes.Post;
import framework.http.responses.JsonResponse;
import framework.http.responses.Response;
import playground.entities.Article;
import playground.repositories.ArticleRepository;
import playground.service.ArticleService;

@Controller( path = "/articles")
public class ArticleController {
    private ArticleRepository articleRepository;
    private ArticleService articleService;

    @Get(path = "/all")
    public Response getAllArticles(){
        return new JsonResponse(articleRepository.getAll());
    }

    @Post(path = "")
    public void addArticle(Article article){
        articleRepository.add(article);
        articleService.calculator.add(1, 2);
        articleService.calculator.subtract(5,4);
    }
}
