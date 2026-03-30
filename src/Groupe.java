import Exceptions.EtudiantNotFoundException;
import Exceptions.GroupePleinException;

import java.util.ArrayList;

public class Groupe {
    final String nomGroupe;
    ArrayList<Etudiant> etudiants = new ArrayList<>();

    public Groupe(String nom) {
        this.nomGroupe = nom;
    }

    public void ajouterEtudiant(Etudiant e)throws GroupePleinException {
        if(etudiants.size()>30) throw new GroupePleinException("Le Groupe est plein");
        if(e != null && !etudiants.contains(e)){
        etudiants.add(e);}
    }

    public void supprimerEtudiant(Etudiant e)throws EtudiantNotFoundException {
        if(!chercherEtudiant(e.getId())) throw new EtudiantNotFoundException("Étudiant non trouvé pour le supprimer");
        etudiants.remove(e);
    }

    public void supprimerEtudiant(String id) throws EtudiantNotFoundException {
        if(!chercherEtudiant(id)) throw new EtudiantNotFoundException("Étudiant non trouvé pour le supprimer");
        etudiants.removeIf(etu->etu.getId().equals(id));
    }

    public boolean chercherEtudiant(String critere) {
        for (Etudiant e : etudiants) {
            if (e.getNom().equals(critere) || e.getId().equals(critere)) {
                return true;
            }
        }
        return false;
    }

    public void afficherEtudiants() {
        System.out.println("Les étudiants de groupe "+ nomGroupe);
        int count=0;
        for(Etudiant e : etudiants){
            count++;
            System.out.println(count+"- "+e.getNom()+" "+e.getPrenom()+" /ID: "+e.getId()+" / Email: "+e.getEmail()+" / Date de Naissance: "+e.getDateNaissance());

        }
    }

    public int getNombreEtudiants(){
        return etudiants.size();
    }

    public boolean estVide() {
        return etudiants.isEmpty();
    }

    public Etudiant getEtudiant(String id) throws EtudiantNotFoundException {
        for(Etudiant e : etudiants){
            if(e.getId().equals(id)) return e;
        }
        throw new EtudiantNotFoundException("Etudiant introuvable");
    }

    public double calculerMoyenneGeneraldeGroupe() {
        return etudiants.stream()
                .mapToDouble(Etudiant::calculerMoyenneGenerale)
                .average()
                .orElse(0.0); // retourne 0 si aucun étudiant
    }
}
