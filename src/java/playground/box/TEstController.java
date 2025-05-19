package playground.box;

import framework.annotations.components.Controller;
import framework.annotations.methodes.Get;

@Controller( path = "/test")
public class TEstController {

    @Get(path = "/all")
    public void getAll() {
        System.out.printf("JANKOO");
    }
}
