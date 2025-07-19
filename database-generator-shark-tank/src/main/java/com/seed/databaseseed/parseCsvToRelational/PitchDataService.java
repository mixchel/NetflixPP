package com.seed.databaseseed.parseCsvToRelational;

import com.seed.databaseseed.entities.relationalModel.*;
import com.seed.databaseseed.entities.PitchData;
import com.seed.databaseseed.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;

@Service
public class PitchDataService {
    Logger logger = Logger.getLogger(PitchDataService.class.getName());

    public List<PitchData> getAllPitches() {
        return CsvProcessor.getPitches();
    }

    @Autowired
    private InvestmentRepository investmentRepository;

    @Autowired
    private EntrepeneurRepository entrepeneurRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SharkRepository sharkRepository;

    @Autowired
    private EpisodeRepository episodeRepository;

    @Transactional
    public void managePitch() {
        List<PitchData> pitches = getAllPitches();
        //Persist all Entity Relational model for each pitch
        for (PitchData p : pitches) {
            Episode episode;
            Project project;
            List<Entrepreneur> entrepreneurs;
            Set<Shark> sharks = p.getSharks();
            episode = setEpisodeToShark(p.getSeason(), p.getEpisode(), sharks);
            project = setEpisodeToProject(p.getProject(), episode);
            entrepreneurs = setProjectToEntrepreneur(project, p.getEntrepreneurNames());

            episodeRepository.save(episode);
            sharkRepository.saveAll(sharks);
            entrepeneurRepository.saveAll(entrepreneurs);
            List<Investment> investments = insertInvestment(project, p.getInvestors(), p.getInvestmentAmountPerShark(), p.getInvestmentAmountPerShark());
            projectRepository.save(project);
            investmentRepository.saveAll(investments);
        }
    }

    private List<Investment> insertInvestment(Project project, Set<Shark> investors, Double investmentAmountPerShark, Double percentageOfCompanyPerShark) {
        List<Investment> investments = new ArrayList<>();
        for (Shark s : investors) {
            Investment investment = new Investment(s, project, investmentAmountPerShark, percentageOfCompanyPerShark);
            investments.add(investment);
            s.addInvestment(investment);
        }
        project.setInvestments(investments);
        return investments;
    }

    private Episode setEpisodeToShark(Integer season, Integer number, Set<Shark> sharks) {
        Episode episode = new Episode(number, season);
        episode.setSharks(sharks);
        for (Shark s : sharks) s.addEpisode(episode);
        return episode;
    }

    private Project setEpisodeToProject(Project project, Episode episode) {
        project.setEpisode(episode);
        episode.addProject(project);
        return project;
    }

    private List<Entrepreneur> setProjectToEntrepreneur(Project project, List<Entrepreneur> entrepreneurs) {
        project.setEntrepreneurs(entrepreneurs);
        for (Entrepreneur e : entrepreneurs) e.setProject(project);
        return entrepreneurs;
    }
}
