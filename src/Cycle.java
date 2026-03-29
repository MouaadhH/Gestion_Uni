import java.util.ArrayList;

public class Cycle {
    String nomCompose;
    Integer duree;
    Integer nbrSemestre;
    String specialite;
    ArrayList<String> nomsModules =new ArrayList<>();
    ArrayList<Section> sections = new ArrayList<>();

    public Cycle(String nomCompose, int duree , int nbrSemestre){
        this.nomCompose=nomCompose;
        this.duree = duree;
        this.nbrSemestre = nbrSemestre;

    }

    //Gestion des Modules
    public void ajouterNomModules(String nom){
        if(nom != null && !nomsModules.contains(nom)) nomsModules.add(nom);
    }

    public void supprimerNomModule(String nom){
        nomsModules.remove(nom);
    }

    public void getNomsModules(){
        nomsModules.stream().forEach(System.out::println);
    }

    //Gestion des sections
    public void ajouterSection(Section s){
        if(s != null && !sections.contains(s)) sections.add(s);
    }

    public void ggetSections() {
        sections.stream().forEach(System.out::println);
    }

}
