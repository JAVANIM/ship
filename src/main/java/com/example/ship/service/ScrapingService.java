package com.example.ship.service;

import com.example.ship.dto.VesselInfoDto;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import com.microsoft.playwright.Frame;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScrapingService {

	public List<VesselInfoDto> searchVessel(String terminal, String keyword) {
	    List<VesselInfoDto> results = new ArrayList<>();

	    if ("BNCT".equalsIgnoreCase(terminal)) {
	        results = scrapeBnctWithPlaywright(keyword);
	    } else if ("DGT".equalsIgnoreCase(terminal)) {
	        results = scrapeDgtWithPlaywright(keyword);
	    } else if ("HJNC".equalsIgnoreCase(terminal)) {
	        results = scrapeHjncWithPlaywright(keyword);
	    } else if ("HPNT".equalsIgnoreCase(terminal)) {
	        results = scrapeHpntWithPlaywright(keyword);
	    } else if ("PNC".equalsIgnoreCase(terminal)) {
	        results = scrapePncWithPlaywright(keyword);
	    } else if ("PNIT".equalsIgnoreCase(terminal)) {
	        results = scrapePnitWithPlaywright(keyword);
	    }

	    return results;
	}

    private List<VesselInfoDto> scrapeBnctWithPlaywright(String keyword) {
        List<VesselInfoDto> list = new ArrayList<>();
        String searchWord = (keyword != null) ? keyword.trim() : "";

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
            );

            Page page = browser.newPage();

            // System.out.println(">>> Playwright로 BNCT 페이지 접속 중...");
            page.navigate("https://info.bnctkorea.com/esvc/vessel/berthScheduleT");

            page.waitForTimeout(2000);

            // 검색어가 있을 경우 입력 및 조회 실행
            if (!searchWord.isEmpty()) {
                if (page.querySelector("input[name='vslNm']") != null) {
                    page.fill("input[name='vslNm']", searchWord);
                    
                    // 엔터키 입력으로 검색 실행
                    page.keyboard().press("Enter");
                    
                    // 조회 결과 반영까지 3초 대기
                    page.waitForTimeout(3000);
                }
            }

            String htmlContent = page.content();
            browser.close();

            // 가져온 HTML 파싱
            Document doc = Jsoup.parse(htmlContent);
            Elements rows = doc.select("table tbody tr, table tr");

            for (Element row : rows) {
                Elements cols = row.select("td");

                // BNCT 실제 테이블 컬럼 개수가 7개 이상인지 확인
                if (cols.size() >= 7) {
                    String berth    = cols.get(0).text().trim(); // 0번: 선석
                    String line     = cols.get(1).text().trim(); // 1번: 선사
                    String trCode   = cols.get(2).text().trim(); // 2번: TR 코드 / 항차
                    String shipName = cols.get(3).text().trim(); // 3번: 선명
                    // cols.get(4)는 '반입마감시한'입니다.
                    String eta      = cols.get(5).text().trim(); // 5번: 접안(예정)일시 (진짜 ETA)
                    String etd      = cols.get(6).text().trim(); // 6번: 출항(예정)일시 (진짜 ETD)

                    if (!shipName.isEmpty() && !shipName.contains("선명") && !shipName.contains("조회된")) {
                        if (searchWord.isEmpty() || shipName.toUpperCase().contains(searchWord.toUpperCase())) {
                            list.add(new VesselInfoDto("BNCT", shipName, trCode, eta, etd, berth));
                        }
                    }
                }
            }

            // System.out.println("=== 필터링 후 결과 개수: " + list.size() + "개 ===");

        } catch (Exception e) {
            System.err.println("Playwright 스크래핑 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }
    private List<VesselInfoDto> scrapeDgtWithPlaywright(String keyword) {
        List<VesselInfoDto> list = new ArrayList<>();
        String searchWord = (keyword != null) ? keyword.trim() : "";

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
            );

            Page page = browser.newPage();

            // System.out.println(">>> Playwright로 DGT 페이지 접속 중...");
            page.navigate("https://info.dgtbusan.com/DGT/esvc/vessel/berthScheduleT");

            page.waitForTimeout(2000);

            // 1. 카테고리/선사 콤보박스 또는 검색 셀렉터 설정 (필요시)
            // 2. 선명 검색창에 검색어 입력
            if (page.querySelector("input[name='vslNm']") != null) {
                page.fill("input[name='vslNm']", searchWord);
                
                // 엔터키 또는 조회 버튼 클릭
                page.keyboard().press("Enter");
                page.waitForTimeout(3000); // 결과 테이블 로딩 대기
            }

            String htmlContent = page.content();
            browser.close();

            // 3. Jsoup 파싱
            Document doc = Jsoup.parse(htmlContent);
            Elements rows = doc.select("table tbody tr, table tr");

            for (Element row : rows) {
                Elements cols = row.select("td");

                // 유효한 DGT 데이터 행은 td가 9개 이상입니다.
                // (헤더나 안내문구 행은 td 개수가 적으므로 자동으로 스킵됨)
                if (cols.size() >= 9) {
                    String berth    = cols.get(0).text().trim(); // 0번: 선석 (B1(S) 등)
                    String line     = cols.get(1).text().trim(); // 1번: 선사코드
                    String trCode   = cols.get(2).text().trim(); // 2번: 모선항차 / TR
                    String shipName = cols.get(3).text().trim(); // 3번: 모선명 (선명)
                    
                    // 4번: 항로, 5번: 반입시작시간, 6번: 반입마감시간
                    String eta      = cols.get(7).text().trim(); // 7번: 접안예정일시 (진짜 ETA)
                    String etd      = cols.get(8).text().trim(); // 8번: 출항예정일시 (진짜 ETD)

                    // 불필요한 행 스킵 및 키워드 필터링
                    if (!shipName.isEmpty() && !shipName.contains("모선명") && !shipName.contains("조회된") && !shipName.contains("Total")) {
                        if (searchWord.isEmpty() || shipName.toUpperCase().contains(searchWord.toUpperCase())) {
                            list.add(new VesselInfoDto("DGT", shipName, trCode, eta, etd, berth));
                        }
                    }
                }
            }

            // System.out.println("=== DGT 파싱 결과 개수: " + list.size() + "개 ===");

        } catch (Exception e) {
            System.err.println("DGT 스크래핑 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }
    
    
    
    // 한진########################
    
    
    private List<VesselInfoDto> scrapeHjncWithPlaywright(String keyword) {
        List<VesselInfoDto> list = new ArrayList<>();
        String searchWord = (keyword != null) ? keyword.trim() : "";

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
            );

            Page page = browser.newPage();

            // System.out.println(">>> Playwright로 HJNC 페이지 접속 중...");
            page.navigate("https://www.hjnc.co.kr/esvc/vessel/berthScheduleT");

            page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
            page.waitForTimeout(2000);

            // HJNC는 전체 목록을 조회한 후 자바 단에서 키워드로 필터링하는 방식이 가장 정확합니다.
            String htmlContent = page.content();
            browser.close();

            // Jsoup 파싱
            Document doc = Jsoup.parse(htmlContent);
            
            Elements rows = doc.select("tbody tr");
            if (rows.isEmpty()) {
                rows = doc.select("table tr");
            }

            for (Element row : rows) {
                Elements cols = row.select("td");

                // HJNC 유효 데이터 행은 td가 12개 이상 존재합니다.
                if (cols.size() >= 12) {
                    String berth    = cols.get(1).text().trim(); // 1번: 선석 (3B, 1B 등)
                    String trCode   = cols.get(3).text().trim(); // 3번: TR / 모선항차
                    String shipName = cols.get(4).text().trim(); // 4번: 선명 (ONE FORTUNE 등)
                    String eta      = cols.get(10).text().trim(); // 10번: 접안예정일시
                    String etd      = cols.get(11).text().trim(); // 11번: 출항예정일시

                    // 불필요한 테이블 헤더 / 안내 행 제외
                    if (!shipName.isEmpty() && !shipName.contains("모선명") && !shipName.contains("선명") 
                        && !shipName.contains("조회된") && !shipName.contains("Total")) {

                        // 자바 2단계 키워드 필터링 (검색어가 없으면 전체, 있으면 해당 선박만)
                        if (searchWord.isEmpty() || shipName.toUpperCase().contains(searchWord.toUpperCase())) {
                            list.add(new VesselInfoDto("HJNC", shipName, trCode, eta, etd, berth));
                        }
                    }
                }
            }

            // System.out.println("=== HJNC 파싱 결과 개수: " + list.size() + "개 ===");

        } catch (Exception e) {
            System.err.println("HJNC 스크래핑 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }
    
    // ###################HPNT
    private List<VesselInfoDto> scrapeHpntWithPlaywright(String keyword) {
        List<VesselInfoDto> list = new ArrayList<>();
        String searchWord = (keyword != null) ? keyword.trim() : "";

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
            );

            Page page = browser.newPage();

            // System.out.println(">>> Playwright로 HPNT 페이지 접속 중...");
            page.navigate("https://www.hpnt.co.kr/infoservice/vessel/vslScheduleList.jsp");

            page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
            page.waitForTimeout(2000);

            String htmlContent = page.content();
            browser.close();

            // Jsoup 파싱
            Document doc = Jsoup.parse(htmlContent);
            Elements rows = doc.select("table tbody tr, table tr");

            for (Element row : rows) {
                Elements cols = row.select("td");

                // HPNT 유효 데이터 행은 td가 10개 이상입니다.
                if (cols.size() >= 10) {
                    String berth    = cols.get(0).text().trim(); // 0번: 선석 (T3(S) 등)
                    String trCode   = cols.get(2).text().trim(); // 2번: 모선항차 / TR (MSEM001 등)
                    String shipName = cols.get(4).text().trim(); // 4번: 선명 (MSC EMMA 등)
                    
                    String eta      = cols.get(8).text().trim(); // 8번: 접안(예정)일시
                    String etd      = cols.get(9).text().trim(); // 9번: 출항(예정)일시

                    // 헤더 및 불필요한 안내 행 제외
                    if (!shipName.isEmpty() && !shipName.contains("모선명") && !shipName.contains("선명") 
                        && !shipName.contains("조회된") && !shipName.contains("Total")) {

                        // 자바 2단계 키워드 필터링
                        if (searchWord.isEmpty() || shipName.toUpperCase().contains(searchWord.toUpperCase())) {
                            list.add(new VesselInfoDto("HPNT", shipName, trCode, eta, etd, berth));
                        }
                    }
                }
            }

            // System.out.println("=== HPNT 파싱 결과 개수: " + list.size() + "개 ===");

        } catch (Exception e) {
            System.err.println("HPNT 스크래핑 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }
    
    // ############PNC
    
    
 private List<VesselInfoDto> scrapePncWithPlaywright(String keyword) {
        List<VesselInfoDto> list = new ArrayList<>();
        String searchWord = (keyword != null) ? keyword.trim() : "";

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
            );

            Page page = browser.newPage();
            page.navigate("https://svc.pncport.com/info/CMS/Ship/Info.pnc?mCode=MN014");

            page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
            page.waitForTimeout(2000);

            // 검색어가 있는 경우 입력창에 타이핑
            if (!searchWord.isEmpty()) {
                if (page.querySelector("input[name='vslNm']") != null) {
                    page.fill("input[name='vslNm']", searchWord);
                }
            }

            // 조회 버튼 클릭 또는 엔터
            try {
                if (page.querySelector("button:has-text('조회'), input[value='조회'], .btn_search") != null) {
                    page.click("button:has-text('조회'), input[value='조회'], .btn_search");
                } else {
                    page.keyboard().press("Enter");
                }
                page.waitForTimeout(4000); // 데이터 로딩 대기
            } catch (Exception e) {
                System.out.println("PNC 조회 버튼 클릭 예외: " + e.getMessage());
            }

            String htmlContent = page.content();
            browser.close();

            Document doc = Jsoup.parse(htmlContent);
            Elements rows = doc.select("table tbody tr, table tr");
            System.out.println(">>> [PNC 디버깅] 감지된 행 개수: " + rows.size());

            for (Element row : rows) {
                Elements cols = row.select("td"); // th 제외하고 td만 추출

                if (cols.size() >= 5) {
                    String shipName = cols.size() > 1 ? cols.get(1).text().trim() : ""; 
                    String trCode   = cols.size() > 2 ? cols.get(2).text().trim() : ""; 
                    String eta      = cols.size() > 7 ? cols.get(7).text().trim() : ""; 
                    String etd      = cols.size() > 8 ? cols.get(8).text().trim() : ""; 
                    String berth    = cols.size() > 9 ? cols.get(9).text().trim() : ""; 

                    if (!shipName.isEmpty() && !shipName.contains("모선명") && !shipName.contains("선명") 
                        && !shipName.contains("조회된") && !shipName.contains("Total")) {

                        if (searchWord.isEmpty() || shipName.toUpperCase().contains(searchWord.toUpperCase())) {
                            list.add(new VesselInfoDto("PNC", shipName, trCode, eta, etd, berth));
                        }
                    }
                }
            }

            System.out.println("=== PNC 최종 파싱 결과 개수: " + list.size() + "개 ===");

        } catch (Exception e) {
            System.err.println("PNC 스크래핑 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }
   // ############PNIT
   // 수정 전: Elements cols = row.select("td, th");
// 수정 후: 데이터 행은 td만 가져오도록 변경
Elements cols = row.select("td");

if (cols.size() >= 5) { // 유효 데이터 컬럼 수 확인
    String rowText = row.text().trim();

    if (rowText.contains("검색된") || rowText.contains("조회된") || rowText.contains("Total") || rowText.contains("접안예정일시") || rowText.contains("모선명")) {
        continue;
    }

    String berth = cols.get(0).text().trim();
    String trCode = cols.size() >= 3 ? cols.get(2).text().trim() : "";
    String shipName = "";
    String eta = "";
    String etd = "";

    // 전체 검색(스페이스) 시 안전하게 선명과 일정 추출
    for (int i = 0; i < cols.size(); i++) {
        String cellText = cols.get(i).text().trim();
        if (!searchWord.isEmpty() && cellText.toUpperCase().contains(searchWord.toUpperCase())) {
            shipName = cellText;
        }
    }

    if (shipName.isEmpty() && cols.size() >= 5) {
        shipName = cols.get(4).text().trim(); // 보통 4번 인덱스가 선명
    }

    if (cols.size() >= 10) {
        eta = cols.get(8).text().trim();
        etd = cols.get(9).text().trim();
    } else if (cols.size() >= 6) {
        eta = cols.get(cols.size() - 2).text().trim();
        etd = cols.get(cols.size() - 1).text().trim();
    }

    if (!shipName.isEmpty() && !shipName.equals("선명")) {
        list.add(new VesselInfoDto("PNIT", shipName, trCode, eta, etd, berth));
    }
}
}
