package com.example.ship.controller;

import com.example.ship.dto.VesselInfoDto;
import com.example.ship.service.ScrapingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SearchController {

    @Autowired
    private ScrapingService scrapingService;

    // 메인 페이지 접속 시 (http://localhost:8080/)
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // 검색 버튼 클릭 시 (http://localhost:8080/search?terminal=...&keyword=...)
    @GetMapping("/search")
    public String search(
            @RequestParam(name = "terminal", required = false) String terminal,
            @RequestParam(name = "keyword", required = false) String keyword,
            Model model) {

        System.out.println("컨트롤러 요청 수신: 터미널=" + terminal + ", 검색어=" + keyword);

        if (terminal != null) {
            // 스크래핑 서비스 호출하여 실제 데이터 가져오기
            List<VesselInfoDto> results = scrapingService.searchVessel(terminal, keyword);
            model.addAttribute("results", results);
        }

        model.addAttribute("selectedTerminal", terminal);
        model.addAttribute("keyword", keyword);

        return "index";
    }
}