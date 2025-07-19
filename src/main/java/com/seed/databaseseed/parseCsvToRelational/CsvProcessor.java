package com.seed.databaseseed.parseCsvToRelational;

import com.seed.databaseseed.entities.PitchData;
import com.seed.databaseseed.entities.relationalModel.Entrepreneur;
import com.seed.databaseseed.entities.relationalModel.Project;
import com.seed.databaseseed.entities.relationalModel.Shark;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.logging.Logger;

@Component
public class CsvProcessor {
    //Class for manage the informations in CSV file and parse each row into a PitchData model
    private static final Logger logger = Logger.getLogger(CsvProcessor.class.getName());
    private static final List<PitchData> pitches = new ArrayList<>();

    public static List<PitchData> getPitches() {
        return pitches;
    }

    public static void createVariables(String[] values) throws RuntimeException {

        Integer season = Integer.parseInt(values[0]);
        Integer pitch = Integer.parseInt(values[2]);
        Integer episode = createEpisodeNumber(Integer.parseInt(values[1]), season);
        String projectName = values[3];
        String category = values[4];
        String description = values[5];
        String website = values[8];
        Double valuation = Double.parseDouble(values[10]);
        Project project = new Project(pitch, projectName, website, valuation, category, description);

        Boolean deal = (values[11].equals("1"));
        Double dealValue = deal ? Double.parseDouble(values[12]) : null;
        Double percentageOfProject = deal ? Double.parseDouble(values[13]) : null;
        Integer numberOfSharksInDeal = deal ? Integer.parseInt(values[15]) : null;
        Double percentageOfCompanyPerShark = deal ? percentageOfProject / numberOfSharksInDeal : null;
        Double investmentAmountPerShark = deal ? dealValue / numberOfSharksInDeal : null;

        Set<Shark> sharks = createSharks(values);
        Set<Shark> investors = createInvestors(values);

        String entrepreneurGender = values[6];
        String namesInTheString = values[7];
        List<String> entrepreneursNames = new ArrayList<>(namesInTheString.contains("_") ? Arrays.asList(namesInTheString.split("_")) : Collections.singletonList(namesInTheString));
        List<Entrepreneur> entrepreneurs = createEntrepreneurs(entrepreneursNames, entrepreneurGender, project);

        pitches.add(new PitchData(episode, season, pitch, projectName, category, description, entrepreneurGender, entrepreneurs, website, valuation,
                deal, dealValue, percentageOfProject, numberOfSharksInDeal, percentageOfCompanyPerShark, investmentAmountPerShark, sharks, investors, project));
    }

    private static List<Entrepreneur> createEntrepreneurs(List<String> names, String gender, Project project) {
        List<Entrepreneur> entrepreneurs = new ArrayList<>();
        for(int i = 0; i < names.size(); i++) {
            entrepreneurs.add(parseEntrepreneur(names.get(i).trim(), gender, project));
        }
        return entrepreneurs;
    }

    private static Entrepreneur parseEntrepreneur(String name, String gender, Project project) {
        Integer code = EntrepeneurCodeManager.getEntrepreneurCode(name);
        return new Entrepreneur(code, name, gender, project);
    }

    private static Integer createEpisodeNumber(Integer episode, Integer season) {
        //Number of episodes of each season:
        //1 = 15
        //2 = 9
        //3 = 15
        if (season == 1) return episode;
        else if (season == 2) return episode + 14;
        else if (season == 3) return episode + 23;
        return episode + 38;
    }

    public static Set<Shark> createSharks(String[] values) {
        Set<Shark> sharks = new HashSet<>();
        for (int i = 43; i < 50; i++) { //43 - 50 are the column numbers that contains the sharks information
            if (Integer.parseInt(values[33]) == 1) sharks.add(new Shark(1, "Barbara Corcoran"));
            if (Integer.parseInt(values[34]) == 1) sharks.add(new Shark(2, "Mark Cuban"));
            if (Integer.parseInt(values[35]) == 1) sharks.add(new Shark(3, "Lori Greiner"));
            if (Integer.parseInt(values[36]) == 1) sharks.add(new Shark(4, "Robert Herjavec"));
            if (Integer.parseInt(values[37]) == 1) sharks.add(new Shark(5, "Daymond John"));
            if (Integer.parseInt(values[38]) == 1) sharks.add(new Shark(6, "Kevin O Leary"));
            if (!values[32].isEmpty()) {
                if (values[32].equals("Kevin Harrington")) sharks.add(new Shark(7, values[32]));
                if (values[32].equals("Jeff Foxworthy")) sharks.add(new Shark(8, values[32]));
            }
        }
        return sharks;
    }

    public static Set<Shark> createInvestors(String[] values) {
        Set<Shark> investors = new HashSet<>();
        for (int i = 18; i < 31; i += 2) { //18 - 31 are the column numbers that contains the investors information
            if (values[18] != "") investors.add(new Shark(1, "Barbara Corcoran"));
            if (values[20] != "") investors.add(new Shark(2, "Mark Cuban"));
            if (values[22] != "") investors.add(new Shark(3, "Lori Greiner"));
            if (values[24] != "") investors.add(new Shark(4, "Robert Herjavec"));
            if (values[26] != "") investors.add(new Shark(5, "Daymond John"));
            if (values[28] != "") investors.add(new Shark(6, "Kevin O Leary"));
            if (values[30] != "" && values[32].equals("Kevin Harrington"))
                investors.add(new Shark(7, "Kevin Harrington"));
            if (values[30] != "" && values[32].equals("Jeff Foxworthy")) investors.add(new Shark(8, "Jeff Foxworthy"));
        }
        return investors;
    }
}
