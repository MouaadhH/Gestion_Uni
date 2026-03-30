import java.util.ArrayList;

public class Semestre {
    protected int  numero;
    ArrayList<String> nomsModules = new ArrayList<>();

    public Semestre(int numero){
        this.numero=numero;
    }

    public void ajouterNomModules(String nom){
        if(nom != null && !nomsModules.stream().anyMatch(s ->s.equals(nom))){
            nomsModules.add(nom);
        }
    }

    public void supprimerNomModule(String nom){
        nomsModules.remove(nom);
    }

    public boolean chercherModule(String nom){
        return nomsModules.contains(nom);
    }

    public void getNomsModules(){
        nomsModules.stream().forEach(System.out::println);
    }

    public int getNombreModules(){
        return nomsModules.size();
    }




}
