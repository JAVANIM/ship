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
  
	
	/**
     * PNIT 스크래핑 메서드 (전체 조회 및 개별 검색 완벽 대응)
     */
    private List<VesselInfoDto> scrapePnitWithPlaywright(String keyword) {
        List<VesselInfoDto> list = new ArrayList<>();
        String searchWord = (keyword != null) ? keyword.trim() : "";

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
            );

            Page page = browser.newPage();
            page.navigate("https://www.pnitl.com/infoservice/vessel/vslScheduleList.jsp");

            page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
            page.waitForTimeout(3000);

            // 전체 조회 시 데이터가 많다면 스크롤을 살짝 내려주거나 대기 시간 확보
            page.waitForTimeout(2000);

            String htmlContent = page.content();
            browser.close();

            Document doc = Jsoup.parse(htmlContent);
            Elements rows = doc.select("tbody tr");
            if (rows.isEmpty()) {
                rows = doc.select("table tr");
            }

            System.out.println(">>> [PNIT 디버깅] 감지된 행 개수: " + rows.size());

            for (Element row : rows) {
                Elements cols = row.select("td");

                // 데이터 셀이 최소 6개 이상인 행만 대상으로 지정
                if (cols.size() >= 6) {
                    String rowText = row.text().trim();
                    
                    // 불필요한 안내 문구 및 헤더 행 스킵
                    if (rowText.contains("검색된") || rowText.contains("조회된") || rowText.contains("Total") 
                        || rowText.contains("접안예정일시") || rowText.contains("모선명") || rowText.contains("선명")
                        || rowText.isEmpty()) {
                        continue;
                    }

                    List<String> cellTexts = new ArrayList<>();
                    for (Element col : cols) {
                        String t = col.text().trim();
                        if (!t.isEmpty()) {
                            cellTexts.add(t);
                        }
                    }

                    // 최소한의 데이터 조각(선석, 선명, 일정 등)이 확보된 경우만 파싱
                    if (cellTexts.size() >= 4) {
                        String berth = cellTexts.get(0);
                        String shipName = "";
                        String trCode = "";
                        String eta = "";
                        String etd = "";

                        // 뒤에서 1~2번째 칸에 날짜/시간 포맷이 있는지 확인하여 ETA, ETD 매핑
                        String lastVal = cellTexts.get(cellTexts.size() - 1);
                        String prevVal = cellTexts.get(cellTexts.size() - 2);

                        if (lastVal.contains("-") || lastVal.contains(":")) {
                            etd = lastVal;
                        }
                        if (prevVal.contains("-") || prevVal.contains(":")) {
                            eta = prevVal;
                        }

                        // 중간 셀들 순회하며 선명과 항차(Voyage/TR Code) 찾기
                        for (int i = 1; i < cellTexts.size() - 2; i++) {
                            String txt = cellTexts.get(i);
                            // 날짜 형식이 아니고, 너무 길지 않은 텍스트 중 항차 코드 혹은 선명 판별
                            if (txt.matches(".*[0-9].*") && txt.length() < 12 && trCode.isEmpty() && !txt.contains("-")) {
                                trCode = txt;
                            } else if (shipName.isEmpty() && !txt.contains("-") && !txt.contains(":")) {
                                shipName = txt;
                            }
                        }

                        // 만약 위 탐색에서 선명이 비었다면 안전하게 고정 인덱스(예: 4번 혹은 3번) 활용
                        if (shipName.isEmpty()) {
                            for (String ct : cellTexts) {
                                // 알파벳이 포함되어 있고 날짜가 아닌 것을 선명으로 간주
                                if (ct.matches(".*[a-zA-Z].*") && !ct.contains("-") && !ct.contains(":")) {
                                    shipName = ct;
                                    break;
                                }
                            }
                        }

                        // 검색어 필터링 검증
                        boolean matchesSearch = searchWord.isEmpty() || 
                            shipName.toUpperCase().contains(searchWord.toUpperCase()) || 
                            trCode.toUpperCase().contains(searchWord.toUpperCase()) ||
                            rowText.toUpperCase().contains(searchWord.toUpperCase());

                        if (!shipName.isEmpty() && matchesSearch) {
                            // 중복 추가 방지 (이미 담긴 데이터가 아니라면 추가)
                            boolean exists = list.stream().anyMatch(v -> v.getShipName().equalsIgnoreCase(shipName) && v.getEta().equals(eta));
                            if (!exists) {
                                list.add(new VesselInfoDto("PNIT", shipName, trCode, eta, etd, berth));
                            }
                        }
                    }
                }
            }

            System.out.println("=== PNIT 최종 파싱 결과 개수: " + list.size() + "개 ===");

        } catch (Exception e) {
            System.err.println("PNIT 스크래핑 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }
}
