package playground.services;

import framework.annotations.components.Autowired;
import framework.annotations.components.Service;
import playground.components.Calculator;

@Service
public class ArticleService {
    @Autowired
    public Calculator calculator;
}
