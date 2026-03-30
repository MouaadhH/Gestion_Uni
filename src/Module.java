import Exceptions.NoteInvalidException;

public class Module {
    private String nom;
    private int credit;
    private Double test;
    private Double exam;
    private Double tp;

    private Double moyenne;
    private Double CC;
    public Module(String nom , int credit){
        this.nom=nom;
        this.credit=credit;
    }

    public Module(String nom, int credit , Double test , Double exam , Double tp ){
        this.nom= nom;
        this.credit=credit;
        this.test=test;
        this.exam=exam;
        this.tp=tp;
    }

    public Double calculerCC(){
        CC = (test + tp) / 2;
        if((test+tp)==0) return 0.0;

        else return (test + tp) / 2;
    }

    public Double calculerMoyenneModule(){
        Double cc = calculerCC();
        moyenne =(cc * 0.4) + (exam * 0.6);
        return (cc * 0.4) + (exam * 0.6);
    }

    //setters
    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public void setExam(Double exam)throws NoteInvalidException {
        if (exam != null && (exam < 0 || exam > 20)) {
            throw new NoteInvalidException("La note doit être entre 0 et 20");
        }
        this.exam = exam;
    }

    public void setTest(Double test)throws NoteInvalidException {
        if (test != null && (test < 0 || test > 20)) {
            throw new NoteInvalidException("La note doit être entre 0 et 20");
        }
        this.test = test;
    }

    public void setTp(Double tp)throws NoteInvalidException {
        if (tp != null && (tp < 0 || tp > 20)) {
            throw new NoteInvalidException("La note doit être entre 0 et 20");
        }
        this.tp = tp;
    }

    //getters
    public String getNom() {
        return nom;
    }

    public Double getExam() {
        return exam;
    }

    public Double getTest() {
        return test;
    }

    public int getCredit() {
        return credit;
    }

    public Double getTp() {
        return tp;
    }

    public Double getCC() {
        return CC;
    }

    public Double getMoyenne() {
        return moyenne;
    }
}
