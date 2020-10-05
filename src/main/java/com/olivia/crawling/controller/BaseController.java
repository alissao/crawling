package com.olivia.crawling.controller;

import com.olivia.crawling.crawlers.BaseCrawler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class BaseController {

    @Autowired
    private BaseCrawler crawler;

    @RequestMapping("/")
    public RedirectView localRedirect() {
        crawl();
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("http://localhost:8080/h2-console");
        return redirectView;
    }

    @Async
    private void crawl() {
        crawler.getPageLinks("https://www.americanas.com.br/mapa-do-site");
    }


}
