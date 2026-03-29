import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Section implements Serializable {
    String nomSection;
    String cycleType;
    int niveau;
    HashMap<String, Groupe> groupes = new HashMap<>();
    HashMap<Integer,Semestre> semesters = new HashMap<>();

    public Section(String nomSection, String cycleType, Integer niveau ){
        this.nomSection=nomSection;
        this.cycleType=cycleType;
        this.niveau=niveau;
    }

    //Gestion des groupes
    public void ajouterGroupe(Groupe g){
        if(g!=null && !groupes.containsKey(g.nomGroupe)) groupes.put(g.nomGroupe,g);
    }

    public Groupe getGroupe(String nomGroupe){
        if(groupes.containsKey(nomGroupe)) return (groupes.get(nomGroupe));
        return null;
    }

    public void listerGroupes(){
       for(Groupe g: groupes.values()){
           System.out.println("-"+g.nomGroupe +"("+g.etudiants.size()+"/30) étudiants");
       }
    }

    //Gestion des étudiantes
    public void ajouterEtudiantDansGroupe(Etudiant e , String nomGroupe) throws GroupePleinException {

        if(groupes.get(nomGroupe).chercherEtudiant(e.getNom())){
            groupes.get(nomGroupe).ajouterEtudiant(e);
        }
    }

    public void supprimerEtudiantDeGroupe(String idEtudiant, String nomGroupe) throws EtudiantNotFoundException {

        groupes.get(nomGroupe).supprimerEtudiant(idEtudiant);
    }

    public void changerEtudiantDeGroupe(String id, String ancienG, String nouveauG) throws EtudiantNotFoundException, GroupePleinException {

       Etudiant e = groupes.get(ancienG).getEtudiant(id);

       supprimerEtudiantDeGroupe(id , ancienG);

       ajouterEtudiantDansGroupe(e,nouveauG);
    }

    public boolean rechercheEtudiant(String critere){
        for(Groupe g : groupes.values()){
            for(Etudiant e : g.etudiants){
                if(e.getId().equals(critere) || e.getNom().equals(critere)){
                    return true;
                }
            }
        }
        return false;
    }

    public Etudiant getEtudiant(String critere) throws EtudiantNotFoundException {
        for(Groupe g : groupes.values()){
            for(Etudiant e : g.etudiants){
                if(e.getId().equals(critere) || e.getNom().equals(critere)){
                    return e;
                }
            }
        }
        throw new EtudiantNotFoundException("Etudiant introuvable");
    }

    //Gestion des modules
    public void affecterModulesAuxEtudiants(int numeroSemestre, String fichierModules) throws FormatFichierInvalidException {
        ArrayList<Module> modules = new ArrayList<>();
        if(!fichierModules.contains("txt")) throw new FormatFichierInvalidException("Fichier mal formate");

        try (BufferedReader br = new BufferedReader(new FileReader(fichierModules+".txt"))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {

                String[] parts = ligne.split(":");// On sépare le nom et le crédit avec " :

                if (parts.length == 2) {
                    String nom = parts[0].trim(); // nom du module
                    int credit = Integer.parseInt(parts[1].trim()); // crédit
                    modules.add(new Module(nom, credit));
                }
            }
        } catch (IOException e) {
            System.out.println("acces aux fichier echouee");
        }

        // Ajouter les modules au semestre
        ArrayList<String> nomsModules = new ArrayList<>(
                modules.stream().map(Module::getNom).toList()
        );
        semesters.get(numeroSemestre).nomsModules.addAll(nomsModules);

        // Ajouter les modules aux étudiants
        for (Groupe g : groupes.values()) {
            for (Etudiant e : g.etudiants) {
                for (Module m : modules) {
                    if (!e.modules.contains(m)) {
                        e.modules.add(m);
                    }
                }
            }
        }


    }

}
