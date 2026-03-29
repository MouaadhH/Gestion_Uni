import java.time.LocalDate;
import java.util.ArrayList;

public class Etudiant {
   private final String id;
   private final String nom;
   private final String prenom;
   private final LocalDate dateNaissance;
   private String email;
   ArrayList<Module> modules = new ArrayList<>();

   public Etudiant(String id, String nom, String prenom, String email, LocalDate dateNaissance) throws DateNaissanceInvalideException, EmailInvalidException {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;

        if(!email.contains("@")){
            throw new EmailInvalidException("Email doit contenir \"@\" ");
        }
        this.email = email;

        if(dateNaissance.isAfter(LocalDate.now())){
            throw new DateNaissanceInvalideException("Date de naissance ne peut pas être dans le futur");
        }
        this.dateNaissance = dateNaissance;
   }

    //setters
    public void setEmail(String email) {
        this.email = email;
    }

    //getters
    public String getNom() {
        return nom;
    }

    public String getId() {
        return id;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    @Override
    public String toString(){
       return"id="+id+
               "Nom="+nom+
               "Prenom="+prenom+
               "Email="+email+
               "Date de naissance="+dateNaissance;
    }

    //primary key specifications
    public boolean equals(Etudiant e){
       return id.equals((e.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    //gestion des modules
    public void ajouterModule(Module m){
       if(m != null && !modules.contains(m) ){modules.add(m);}
    }

    public void getModules(){
      modules.stream().forEach(System.out::println);
    }

    public Module getModule(String nomModule) throws ModuleNotFound {
       for(Module m : modules){
           if(nomModule.equals(m.getNom())) return m;
       }
       throw new ModuleNotFound("Module introuvable");
    }

    public Double calculerMoyenneGenerale(){
       double moyenne = 0.0;
       double sommecredit=0.0;
       for(Module m : modules){
           moyenne+=(m.getMoyenne()*m.getCredit());
           sommecredit+=m.getCredit();
       }
       if(sommecredit ==0 ) throw new ArithmeticException("Aucun module pour calculer la moyenne");

       moyenne=moyenne/sommecredit;
       return moyenne;

    }

}
