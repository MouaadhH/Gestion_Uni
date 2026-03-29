import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class GestionUniversitaire {

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);
        Section section = new Section("..." ,"...",1);
        Cycle curr_cycle = null;

        HashMap<String, Cycle> cycles = new HashMap<>();

        int choixCycle;

        // CONFIGURATION DU CYCLE
        do {
            System.out.println("===========================================================");
            System.out.println("GESTION UNIVERSITAIRE - Application");
            System.out.println("Gestion complete des etudiants");
            System.out.println("===========================================================");

            System.out.println("CONFIGURATION DU CYCLE D'ETUDES");
            System.out.println("Pour creer un Cycle (1,2,3,4)");
            System.out.println("1. Ingénieur (5 ans, 10 semestres)");
            System.out.println("2. Licence   (3 ans, 6 semestres)");
            System.out.println("3. Master    (2 ans, 4 semestres)");
            System.out.println("4. Doctorat  (3 ans, 6 semestres)");
            System.out.println("5. Gérer un cycle existant");
            System.out.println("6. Quitter");
            System.out.print("Votre choix: ");

            choixCycle = scanner.nextInt();
            scanner.nextLine();

            switch (choixCycle) {
                case 1:
                    curr_cycle = new Cycle("Ingenieur", 5, 10);
                    break;
                case 2:
                    curr_cycle = new Cycle("Licence", 3, 6);
                    break;
                case 3:
                    curr_cycle = new Cycle("Master", 2, 4);
                    break;
                case 4:
                    curr_cycle = new Cycle("Doctorat", 3, 6);
                    break;
                case 5:
                    String nomCycle;
                    do {
                        System.out.println("Entrer le nom du cycle :");
                        nomCycle = scanner.nextLine().trim();
                        if (nomCycle.isEmpty()) System.out.println("Ce champ est obligatoire.");
                    } while (nomCycle.isEmpty());
                    curr_cycle = cycles.get(nomCycle);
                    if (curr_cycle == null) {
                        System.out.println("Cycle introuvable.");
                        continue;
                    }
                    break;
                case 6:
                    System.out.println("\nAu revoir ;)");
                    scanner.close();
                    return;
                default:
                    System.out.println("Choix invalide !");
                    continue;
            }

            if (choixCycle != 5) {
                String specialite;
                do {
                    System.out.print("Specialite: ");
                    specialite = scanner.nextLine().trim();
                    if (specialite.isEmpty()) System.out.println("Ce champ est obligatoire.");
                } while (specialite.isEmpty());
                curr_cycle.specialite = specialite;

                System.out.println("Cycle cree avec succes");
                System.out.println(curr_cycle.nomCompose + " en " + specialite);
                System.out.println("Duree: " + curr_cycle.duree + " ans (" + curr_cycle.nbrSemestre + " semestres)");

                cycles.put(curr_cycle.nomCompose, curr_cycle);

                String c;
                do {
                    System.out.print("Voulez-vous importer les modules depuis des fichiers? (o/n): ");
                    c = scanner.nextLine().trim();
                    if (c.isEmpty()) System.out.println("Ce champ est obligatoire.");
                } while (c.isEmpty());
                if (c.equals("o")) {
                    // TODO: import from file
                }
            } else {
                System.out.println(curr_cycle.nomCompose + " en " + curr_cycle.specialite);
                System.out.println("Duree: " + curr_cycle.duree + " ans (" + curr_cycle.nbrSemestre + " semestres)");

                String c;
                do {
                    System.out.print("Voulez-vous importer les modules depuis des fichiers? (o/n): ");
                    c = scanner.nextLine().trim();
                    if (c.isEmpty()) System.out.println("Ce champ est obligatoire.");
                } while (c.isEmpty());
                if (c.equals("o")) {
                    // TODO: import from file
                }
            }

            // CONFIGURATION SECTION
            System.out.println("CONFIGURATION DE LA SECTION");
            String choixsec;
            do {
                System.out.println("Creer une section (c) ou gérer une section existante (e): ");
                choixsec = scanner.nextLine().trim();
                if (choixsec.isEmpty()) System.out.println("Ce champ est obligatoire.");
            } while (choixsec.isEmpty());

            if (choixsec.equals("c")) {
                String nomSection;
                do {
                    System.out.print("Nom de la section: ");
                    nomSection = scanner.nextLine().trim();
                    if (nomSection.isEmpty()) System.out.println("Ce champ est obligatoire.");
                } while (nomSection.isEmpty());

                int niveau = 0;
                do {
                    System.out.println("Niveau d'etude:");
                    System.out.println("1. 1ere annee");
                    for (int i = 2; i <= curr_cycle.duree; i++) {
                        System.out.println(i + ". " + i + "eme annee");
                    }
                    niveau = scanner.nextInt();
                    scanner.nextLine();
                    if (niveau < 1 || niveau > curr_cycle.duree)
                        System.out.println("Niveau invalide, veuillez reessayer.");
                } while (niveau < 1 || niveau > curr_cycle.duree);

                section = new Section(nomSection, curr_cycle.nomCompose + curr_cycle.specialite, niveau);

                System.out.println("Section cree avec succes!");
                if (niveau > 1) {
                    System.out.println(nomSection + " - " + curr_cycle.nomCompose + " " + niveau + "eme annee");
                } else {
                    System.out.println(nomSection + " - " + curr_cycle.nomCompose + " " + niveau + "ere annee");
                }

                curr_cycle.sections.add(section);

            } else if (choixsec.equals("e")) {
                String nomsec;
                do {
                    System.out.println("Le nom de section: ");
                    nomsec = scanner.nextLine().trim();
                    if (nomsec.isEmpty()) System.out.println("Ce champ est obligatoire.");
                } while (nomsec.isEmpty());

                for (Section S : curr_cycle.sections) {
                    if (nomsec.equals(S.nomSection)) section = S;
                }
                if (section == null) throw new Exception("Section introuvable");

                if (section.niveau > 1) {
                    System.out.println(section.nomSection + " - " + curr_cycle.nomCompose + " " + section.niveau + "eme annee");
                } else {
                    System.out.println(section.nomSection + " - " + curr_cycle.nomCompose + " " + section.niveau + "ere annee");
                }
            }

            int choix = 0;

            // MENU PRINCIPAL
            do {
                System.out.println("\n===========================================================");
                System.out.println("MENU PRINCIPAL");
                System.out.println("===========================================================");
                System.out.println("1. Gestion des étudiants");
                System.out.println("2. Gestion des groupes");
                System.out.println("3. Gestion des modules et notes");
                System.out.println("4. Gestion des semestres");
                System.out.println("5. Affecter les modules (depuis fichier)");
                System.out.println("6. Rechercher un etudiant");
                System.out.println("7. Statistiques");
                System.out.println("8. Sauvegarder la section");
                System.out.println("9. Charger une section");
                System.out.println("10. Quitter");
                System.out.print("Votre choix: ");

                choix = scanner.nextInt();
                scanner.nextLine();

                switch (choix) {

                    case 1:
                        do {
                            System.out.println("-----------------------------------------------------------");
                            System.out.println("GESTION DES ETUDIANTS");
                            System.out.println("-----------------------------------------------------------");
                            System.out.println("1. Ajouter un etudiant");
                            System.out.println("2. Modifier un etudiant");
                            System.out.println("3. Supprimer un etudiant");
                            System.out.println("4. Rechercher un etudiant");
                            System.out.println("5. Lister tous les etudiants");
                            System.out.println("6. Retour");
                            System.out.println("-----------------------------------------------------------");
                            System.out.print("Votre choix: ");

                            choix = scanner.nextInt();
                            scanner.nextLine();

                            switch (choix) {

                                case 1:
                                    System.out.println("--- INSCRIPTION D'UN ETUDIANT ---");
                                    System.out.println("Section: " + section.nomSection);
                                    System.out.println("Cycle: " + section.cycleType + " en " + curr_cycle.specialite);
                                    System.out.println("Niveau: " + section.niveau + "ere annee");

                                    String nomGroupe;
                                    if (section.groupes.isEmpty()) {
                                        System.out.println("Creer un groupe (aucun groupe trouve)");
                                        do {
                                            System.out.println("Nom groupe pour le creer : ");
                                            nomGroupe = scanner.nextLine().trim();
                                            if (nomGroupe.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                        } while (nomGroupe.isEmpty());
                                        Groupe groupe = new Groupe(nomGroupe);
                                        section.ajouterGroupe(groupe);
                                    } else {
                                        section.listerGroupes();
                                        do {
                                            System.out.print("Nom de Groupe: ");
                                            nomGroupe = scanner.nextLine().trim();
                                            if (nomGroupe.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                            if (section.groupes.get(nomGroupe).etudiants.size()>30) System.out.println("Ce groupe est plein, veuillez choisir un autre groupe.");
                                        } while (nomGroupe.isEmpty() || section.groupes.get(nomGroupe).etudiants.size()>30 );
                                    }

                                    System.out.println("Informations personnelles:");

                                    String id;
                                    do {
                                        System.out.print("ID: ");
                                        id = scanner.nextLine().trim();
                                        if (id.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (id.isEmpty());

                                    String nom;
                                    do {
                                        System.out.print("Nom: ");
                                        nom = scanner.nextLine().trim();
                                        if (nom.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (nom.isEmpty());

                                    String prenom;
                                    do {
                                        System.out.print("Prenom: ");
                                        prenom = scanner.nextLine().trim();
                                        if (prenom.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (prenom.isEmpty());

                                    String email;
                                    do {
                                        System.out.print("Email: ");
                                        email = scanner.nextLine().trim();
                                        if (email.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (email.isEmpty());

                                    LocalDate dateNaissance = null;
                                    do {
                                        try {
                                            System.out.print("Date de naissance (AAAA-MM-JJ): ");
                                            String dateStr = scanner.nextLine().trim();
                                            if (dateStr.isEmpty()) {
                                                System.out.println("Ce champ est obligatoire.");
                                                continue;
                                            }
                                            dateNaissance = LocalDate.parse(dateStr);
                                        } catch (Exception e) {
                                            System.out.println("Format de date invalide. Utilisez AAAA-MM-JJ.");
                                        }
                                    } while (dateNaissance == null);

                                    Etudiant etudiant = new Etudiant(id, nom, prenom, email, dateNaissance);
                                    section.ajouterEtudiantDansGroupe(etudiant, nomGroupe);
                                    System.out.println("INSCRIPTION REUSSIE!");
                                    break;

                                case 2:
                                    String idMod;
                                    do {
                                        System.out.print("ID de l'etudiant a modifier: ");
                                        idMod = scanner.nextLine().trim();
                                        if (idMod.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (idMod.isEmpty());

                                    String ancienG;
                                    do {
                                        System.out.println("Le nom de l'ancien groupe:");
                                        ancienG = scanner.nextLine().trim();
                                        if (ancienG.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (ancienG.isEmpty());

                                    String nouveauG;
                                    do {
                                        System.out.println("Le nom du nouveau groupe:");
                                        nouveauG = scanner.nextLine().trim();
                                        if (nouveauG.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (nouveauG.isEmpty());

                                    section.changerEtudiantDeGroupe(idMod, ancienG, nouveauG);
                                    break;

                                case 3:
                                    String idSup;
                                    do {
                                        System.out.print("ID de l'etudiant a supprimer: ");
                                        idSup = scanner.nextLine().trim();
                                        if (idSup.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (idSup.isEmpty());

                                    String nomGsup;
                                    do {
                                        System.out.println("Le nom du groupe:");
                                        nomGsup = scanner.nextLine().trim();
                                        if (nomGsup.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (nomGsup.isEmpty());

                                    section.supprimerEtudiantDeGroupe(idSup, nomGsup);
                                    System.out.println("Etudiant supprime.");
                                    break;

                                case 4:
                                    String critere;
                                    do {
                                        System.out.print("Critere de recherche (ID, nom): ");
                                        critere = scanner.nextLine().trim();
                                        if (critere.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (critere.isEmpty());

                                    if (section.rechercheEtudiant(critere)) {
                                        System.out.println("Etudiant trouve");
                                    } else {
                                        System.out.println("Etudiant non trouve");
                                    }
                                    break;

                                case 5:
                                    for (Groupe g : section.groupes.values()) {
                                        g.afficherEtudiants();
                                    }
                                    break;

                                case 6:
                                    System.out.println("Retour au menu principal...");
                                    break;

                                default:
                                    System.out.println("Choix invalide !");
                            }

                        } while (choix != 6);
                        break;

                    case 2:
                        do {
                            System.out.println("-----------------------------------------------------------");
                            System.out.println("GESTION DES GROUPES");
                            System.out.println("-----------------------------------------------------------");
                            System.out.println("1. Creer un groupe");
                            System.out.println("2. Retour");
                            System.out.println("------------------------------------------------------------");
                            System.out.print("Votre choix: ");

                            choix = scanner.nextInt();
                            scanner.nextLine();

                            if (choix == 1) {
                                String nomGroupe;
                                do {
                                    System.out.println("Donner le nom du groupe:");
                                    nomGroupe = scanner.nextLine().trim();
                                    if (nomGroupe.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                } while (nomGroupe.isEmpty());

                                Groupe groupeObj = new Groupe(nomGroupe);
                                String ch = "";
                                do {
                                    String idG;
                                    do {
                                        System.out.print("Id: ");
                                        idG = scanner.nextLine().trim();
                                        if (idG.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (idG.isEmpty());

                                    String nomG;
                                    do {
                                        System.out.print("Nom: ");
                                        nomG = scanner.nextLine().trim();
                                        if (nomG.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (nomG.isEmpty());

                                    String prenomG;
                                    do {
                                        System.out.print("Prenom: ");
                                        prenomG = scanner.nextLine().trim();
                                        if (prenomG.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (prenomG.isEmpty());

                                    String emailG;
                                    do {
                                        System.out.print("Email: ");
                                        emailG = scanner.nextLine().trim();
                                        if (emailG.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (emailG.isEmpty());

                                    LocalDate dateNaissanceG = null;
                                    do {
                                        try {
                                            System.out.print("Date de Naissance (AAAA-MM-JJ): ");
                                            String dateStr = scanner.nextLine().trim();
                                            if (dateStr.isEmpty()) {
                                                System.out.println("Ce champ est obligatoire.");
                                                continue;
                                            }
                                            dateNaissanceG = LocalDate.parse(dateStr);
                                        } catch (Exception e) {
                                            System.out.println("Format de date invalide. Utilisez AAAA-MM-JJ.");
                                        }
                                    } while (dateNaissanceG == null);

                                    Etudiant etudiantG = new Etudiant(idG, nomG, prenomG, emailG, dateNaissanceG);
                                    groupeObj.ajouterEtudiant(etudiantG);

                                    do {
                                        System.out.println("Creer autre étudiant (C) / Quitter (Q)");
                                        ch = scanner.nextLine().trim();
                                        if (ch.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (ch.isEmpty());

                                } while (!ch.equalsIgnoreCase("Q"));

                                section.ajouterGroupe(groupeObj);

                            } else if (choix != 2) {
                                System.out.println("Choix invalide !");
                            }
                        } while (choix != 2);
                        break;

                    case 3:
                        do {
                            System.out.println("-----------------------------------------------------------");
                            System.out.println("GESTION DES MODULES ET NOTES");
                            System.out.println("-----------------------------------------------------------");
                            System.out.println("1. Creer un module pour un etudiant");
                            System.out.println("2. Saisir une note");
                            System.out.println("3. Calculer le CC d'un etudiant");
                            System.out.println("4. Calculer la moyenne d'un etudiant");
                            System.out.println("5. Afficher le releve de notes");
                            System.out.println("6. Retour");
                            System.out.println("-----------------------------------------------------------");
                            System.out.print("Votre choix: ");

                            choix = scanner.nextInt();
                            scanner.nextLine();

                            switch (choix) {

                                case 1:
                                    String idm;
                                    do {
                                        System.out.print("ID de l'etudiant: ");
                                        idm = scanner.nextLine().trim();
                                        if (idm.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (idm.isEmpty());

                                    String nomModule;
                                    do {
                                        System.out.print("Nom du module: ");
                                        nomModule = scanner.nextLine().trim();
                                        if (nomModule.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (nomModule.isEmpty());

                                    System.out.print("Credit du module: ");
                                    int creditModule = scanner.nextInt();
                                    scanner.nextLine();

                                    Module module = new Module(nomModule, creditModule);
                                    if (section != null) {
                                        section.getEtudiant(idm).ajouterModule(module);
                                    }
                                    break;

                                case 2:
                                    String id2;
                                    do {
                                        System.out.print("ID de l'étudiant: ");
                                        id2 = scanner.nextLine().trim();
                                        if (id2.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (id2.isEmpty());

                                    String nomModul;
                                    do {
                                        System.out.print("Nom du module: ");
                                        nomModul = scanner.nextLine().trim();
                                        if (nomModul.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (nomModul.isEmpty());

                                    System.out.print("Type de note:");
                                    System.out.println("1. Test");
                                    System.out.println("2. Examen");
                                    System.out.println("3. TP");

                                    int type = scanner.nextInt();
                                    scanner.nextLine();

                                    double note;
                                    do {
                                        System.out.print("Note (0-20): ");
                                        note = scanner.nextDouble();
                                        scanner.nextLine();
                                        if (note < 0 || note > 20) System.out.println("La note doit etre entre 0 et 20.");
                                    } while (note < 0 || note > 20);

                                    if (section != null) {
                                        if (type == 1) {
                                            section.getEtudiant(id2).getModule(nomModul).setTest(note);
                                        } else if (type == 2) {
                                            section.getEtudiant(id2).getModule(nomModul).setExam(note);
                                        } else if (type == 3) {
                                            section.getEtudiant(id2).getModule(nomModul).setTp(note);
                                        }
                                    }
                                    break;

                                case 3:
                                    String idCC;
                                    do {
                                        System.out.print("ID de l'etudiant: ");
                                        idCC = scanner.nextLine().trim();
                                        if (idCC.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (idCC.isEmpty());

                                    String moduleCC;
                                    do {
                                        System.out.print("Nom du module: ");
                                        moduleCC = scanner.nextLine().trim();
                                        if (moduleCC.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (moduleCC.isEmpty());

                                    double cc = 0.0;
                                    if (section != null) {
                                        cc = section.getEtudiant(idCC).getModule(moduleCC).calculerCC();
                                    }
                                    System.out.println("Le CC de l'étudiant (id=" + idCC + ") est " + cc + "/20");
                                    break;

                                case 4:
                                    String idMoy;
                                    do {
                                        System.out.print("ID de l'etudiant: ");
                                        idMoy = scanner.nextLine().trim();
                                        if (idMoy.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (idMoy.isEmpty());

                                    String moduleMoy;
                                    do {
                                        System.out.print("Nom du module: ");
                                        moduleMoy = scanner.nextLine().trim();
                                        if (moduleMoy.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (moduleMoy.isEmpty());

                                    double moy = 0.0;
                                    if (section != null) {
                                        moy = section.getEtudiant(idMoy).getModule(moduleMoy).calculerMoyenneModule();
                                    }
                                    System.out.println("La moyenne de l'étudiant (id=" + idMoy + ") est " + moy + "/20");
                                    break;

                                case 5:
                                    String idReleve;
                                    do {
                                        System.out.print("ID de l'etudiant: ");
                                        idReleve = scanner.nextLine().trim();
                                        if (idReleve.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (idReleve.isEmpty());

                                    Etudiant etu = section.getEtudiant(idReleve);

                                    System.out.println("===========================================================");
                                    System.out.println("RELEVE DE NOTES");
                                    System.out.println("===========================================================");
                                    System.out.println("Etudiant: " + etu.getNom());
                                    System.out.println("ID: " + etu.getId());
                                    System.out.println("Email: " + etu.getEmail());
                                    System.out.println("===========================================================");

                                    for (Module m : etu.modules) {
                                        System.out.println("-----------------------------------------------------------");
                                        System.out.println(m.getNom() + " (" + m.getCredit() + " credits)");

                                        Double test = m.getTest();
                                        Double tp = m.getTp();
                                        Double examen = m.getExam();

                                        System.out.println("Test   : " + (test == null ? "---" : test));
                                        System.out.println("TP     : " + (tp == null ? "---" : tp));
                                        System.out.println("Examen : " + (examen == null ? "---" : examen));
                                        System.out.println("Moyenne: " + m.calculerMoyenneModule());
                                    }

                                    System.out.println("===========================================================");
                                    System.out.printf("MOYENNE GENERALE: %.2f/20%n", etu.calculerMoyenneGenerale());
                                    System.out.println("===========================================================");
                                    break;

                                case 6:
                                    System.out.println("Retour au menu principal...");
                                    break;

                                default:
                                    System.out.println("Choix invalide !");
                            }

                        } while (choix != 6);
                        break;

                    case 4:
                        int choixGestionS;


                        do {
                            System.out.println("-----------------------------------------------------------");
                            System.out.println("GESTION DES SEMESTRES");
                            System.out.println("-----------------------------------------------------------");
                            System.out.println("1. Creer un semestre");
                            System.out.println("2. Ajouter un module a un semestre");
                            System.out.println("3. Supprimer un module d'un semestre");
                            System.out.println("4. Lister les modules d'un semestre");
                            System.out.println("5. Lister tous les semestres");
                            System.out.println("6. Retour");
                            System.out.println("-----------------------------------------------------------");
                            System.out.print("Votre choix: ");

                            choixGestionS = scanner.nextInt();
                            scanner.nextLine();

                            switch (choixGestionS) {

                                case 1:

                                    System.out.print("Numero du semestre: ");
                                    int numSem = scanner.nextInt();
                                    scanner.nextLine();
                                    Semestre sem = new Semestre(numSem);
                                    section.semesters.put(numSem,sem);
                                    System.out.println("Semestre " + numSem + " cree.");
                                    break;

                                case 2:
                                    System.out.print("Numero du semestre: ");
                                    int num = scanner.nextInt();
                                    scanner.nextLine();

                                    String moduleS;
                                    do {
                                        System.out.print("Nom du module: ");
                                        moduleS = scanner.nextLine().trim();
                                        if (moduleS.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (moduleS.isEmpty());

                                    boolean validation = false;
                                    for (Semestre s : section.semesters.values()) {
                                        if (num == s.numero) {
                                            s.ajouterNomModules(moduleS);
                                            validation = true;
                                        }
                                    }
                                    if (!validation) throw new SemestreInvalideException("Semestre non trouve");
                                    System.out.println("Module '" + moduleS + "' ajoute au semestre " + num);
                                    break;

                                case 3:
                                    System.out.print("Numero du semestre: ");
                                    int semSup = scanner.nextInt();
                                    scanner.nextLine();

                                    String moduleSup;
                                    do {
                                        System.out.print("Nom du module a supprimer: ");
                                        moduleSup = scanner.nextLine().trim();
                                        if (moduleSup.isEmpty()) System.out.println("Ce champ est obligatoire.");
                                    } while (moduleSup.isEmpty());

                                    boolean validatio = false;
                                    for (Semestre s : section.semesters.values()) {
                                        if (semSup == s.numero) {
                                            s.nomsModules.remove(moduleSup);
                                            validatio = true;
                                        }
                                    }
                                    if (!validatio) throw new SemestreInvalideException("Semestre non trouve");
                                    System.out.println("Module supprime.");
                                    break;

                                case 4:
                                    System.out.print("Numero du semestre: ");
                                    int semList = scanner.nextInt();
                                    scanner.nextLine();

                                    boolean valid = false;
                                    for (Semestre s : section.semesters.values()) {
                                        if (semList == s.numero) {
                                            s.getNomsModules();
                                            valid = true;
                                        }
                                    }
                                    if (!valid) throw new SemestreInvalideException("Semestre non trouve");
                                    break;

                                case 5:

                                    for(Semestre s : section.semesters.values()){
                                        System.out.println("-Semestre "+s.numero+":");
                                        System.out.println("Modules:");
                                        for(String m : s.nomsModules){
                                            System.out.println("-"+m);
                                        }
                                    }

                                    break;

                                case 6:
                                    System.out.println("Retour au menu principal...");
                                    break;

                                default:
                                    System.out.println("Choix invalide !");
                            }

                        } while (choixGestionS != 6);
                        break;

                    case 5:
                        String nomfichie;
                        do {
                            System.out.println("Donner le nom de fichier");
                            nomfichie = scanner.nextLine().trim();
                            if (nomfichie.isEmpty()) System.out.println("Ce champ est obligatoire.");
                        } while (nomfichie.isEmpty());
                        SaveLoad.lireModulesDepuisFichier(nomfichie);
                        break;

                    case 6:
                        String critere;
                        do {
                            System.out.println("Donner l'id ou le nom d'étudiant :");
                            critere = scanner.nextLine().trim();
                            if (critere.isEmpty()) System.out.println("Ce champ est obligatoire.");
                        } while (critere.isEmpty());

                        Etudiant etudiantSearch = section.getEtudiant(critere);
                        System.out.println(etudiantSearch.toString());
                        break;

                    case 7:
                        System.out.println("==============================================================");
                        System.out.println("                       STATISTIQUES                          ");
                        System.out.println("==============================================================");
                        System.out.println("Nom Cycle : " + curr_cycle.nomCompose + " en " + curr_cycle.specialite);
                        if (section.niveau > 1) {
                            System.out.println(section.nomSection + " " + section.niveau + "eme annee");
                        } else {
                            System.out.println(section.nomSection + " " + section.niveau + "ere annee");
                        }
                        System.out.println("==============================================================");
                        System.out.println("Groupes:");
                        int totalEtudiant = 0;
                        for (Groupe g : section.groupes.values()) {
                            System.out.println("- " + g.nomGroupe + "  : " + g.etudiants.size() + " étudiants");
                            totalEtudiant += g.etudiants.size();
                        }
                        System.out.println("==============================================================");
                        System.out.println("Total étudiants: " + totalEtudiant);
                        System.out.println("Moyenne generale par groupe:");
                        for (Groupe g : section.groupes.values()) {
                            System.out.println("- " + g.nomGroupe + "  : " + g.calculerMoyenneGeneraldeGroupe() + "/20");
                        }
                        System.out.println("==============================================================");
                        break;

                    case 8:
                        SaveLoad.saveToFile(section);
                        System.out.println("Section sauvegardee dans " + section.nomSection + ".dat");
                        break;

                    case 9:
                        String nomLoad;
                        do {
                            System.out.print("Nom section a charger: ");
                            nomLoad = scanner.nextLine().trim();
                            if (nomLoad.isEmpty()) System.out.println("Ce champ est obligatoire.");
                        } while (nomLoad.isEmpty());
                        section = SaveLoad.loadFromFile(nomLoad);
                        if (section == null) {
                            throw new FichierIntrouvableException("Fichier introuvable");
                        }
                        System.out.println("Section chargee avec succes!\n" +
                                section.nomSection + " - " + section.cycleType + " (" + section.niveau + " ans)");
                        break;

                    case 10:
                        System.out.println("\nAu revoir ;)");
                        break;

                    default:
                        System.out.println("Choix invalide!");
                }

            } while (choix != 10);

        } while (choixCycle != 6);

        scanner.close();
    }
}