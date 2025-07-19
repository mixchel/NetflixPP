package com.seed.databaseseed.entities;

import com.seed.databaseseed.entities.relationalModel.Entrepreneur;
import com.seed.databaseseed.entities.relationalModel.Project;
import com.seed.databaseseed.entities.relationalModel.Shark;

import java.util.List;
import java.util.Set;

//Store the information of each row in CSV file
public class PitchData {
    private Integer episode;
    private Integer season;
    private Integer pitch;
    private String projectName;
    private String category;
    private String description;
    private String entrepreneurGender;
    private String website;
    private Double valuation;
    private Boolean deal;
    private Double dealValue;
    private Double percentageOfProject;
    private Integer numberOfSharksInDeal;
    private Double percentageOfCompanyPerShark;
    private Double investmentAmountPerShark;
    private Set<Shark> sharks;
    private Set<Shark> investors;
    private List<Entrepreneur> entrepreneurNames;
    private Project project;

    public PitchData() {
    }

    //Use builder?
    public PitchData(Integer episode,
                     Integer season,
                     Integer pitch,
                     String projectName,
                     String category,
                     String description,
                     String entrepreneurGender,
                     List<Entrepreneur> entrepreneurNames,
                     String website,
                     Double valuation,
                     Boolean deal,
                     Double dealValue,
                     Double percentageOfProject,
                     Integer numberOfSharksInDeal,
                     Double percentageOfCompanyPerShark,
                     Double investmentAmountPerShark,
                     Set<Shark> sharks,
                     Set<Shark> investors,
                     Project project) {
        this.episode = episode;
        this.season = season;
        this.pitch = pitch;
        this.projectName = projectName;
        this.category = category;
        this.description = description;
        this.entrepreneurGender = entrepreneurGender;
        this.entrepreneurNames = entrepreneurNames;
        this.website = website;
        this.valuation = valuation;
        this.deal = deal;
        this.dealValue = dealValue;
        this.percentageOfProject = percentageOfProject;
        this.numberOfSharksInDeal = numberOfSharksInDeal;
        this.percentageOfCompanyPerShark = percentageOfCompanyPerShark;
        this.investmentAmountPerShark = investmentAmountPerShark;
        this.sharks = sharks;
        this.investors = investors;
        this.project = project;
    }

    public Integer getEpisode() {
        return episode;
    }

    public Integer getSeason() {
        return season;
    }

    public Integer getPitch() {
        return pitch;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getEntrepreneurGender() {
        return entrepreneurGender;
    }

    public List<Entrepreneur> getEntrepreneurNames() {
        return entrepreneurNames;
    }

    public String getWebsite() {
        return website;
    }

    public Double getValuation() {
        return valuation;
    }

    public Boolean getDeal() { return deal; }

    public Double getDealValue() {
        return dealValue;
    }

    public Double getPercentageOfProject() {
        return percentageOfProject;
    }

    public Integer getNumberOfSharksInDeal() {
        return numberOfSharksInDeal;
    }

    public Double getPercentageOfCompanyPerShark() {
        return percentageOfCompanyPerShark;
    }

    public Double getInvestmentAmountPerShark() {
        return investmentAmountPerShark;
    }

    public Set<Shark> getInvestors() {
        return investors;
    }

    public Set<Shark> getSharks() {
        return sharks;
    }

    public Project getProject() {
        return project;
    }
}