package com.example.ship.dto;

public class VesselInfoDto {
    private String terminal;   // 터미널명
    private String shipName;   // 선명
    private String trCode;     // TR 정보
    private String eta;        // 입항 예정 시각
    private String etd;        // 출항 예정 시각
    private String berth;      // 접안 선석

    // 기본 생성자
    public VesselInfoDto() {}

    // 필드 생성자
    public VesselInfoDto(String terminal, String shipName, String trCode, String eta, String etd, String berth) {
        this.terminal = terminal;
        this.shipName = shipName;
        this.trCode = trCode;
        this.eta = eta;
        this.etd = etd;
        this.berth = berth;
    }

    // Getter / Setter
    public String getTerminal() { return terminal; }
    public void setTerminal(String terminal) { this.terminal = terminal; }

    public String getShipName() { return shipName; }
    public void setShipName(String shipName) { this.shipName = shipName; }

    public String getTrCode() { return trCode; }
    public void setTrCode(String trCode) { this.trCode = trCode; }

    public String getEta() { return eta; }
    public void setEta(String eta) { this.eta = eta; }

    public String getEtd() { return etd; }
    public void setEtd(String etd) { this.etd = etd; }

    public String getBerth() { return berth; }
    public void setBerth(String berth) { this.berth = berth; }
}