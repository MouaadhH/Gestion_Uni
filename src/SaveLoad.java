import Exceptions.FichierIntrouvableException;
import Exceptions.FormatFichierInvalidException;

import java.io.*;
import java.util.ArrayList;

public class SaveLoad {

    public static void saveToFile(Section section) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(section.nomSection + ".dat"))) {
            out.writeObject(section);
        }
    }

    public static Section loadFromFile(String nomSection) throws FichierIntrouvableException {
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(nomSection + ".dat"))) {
            return (Section) in.readObject();
        } catch (FileNotFoundException e) {
            throw new FichierIntrouvableException("Fichier introuvable: " + nomSection + ".dat");
        } catch (IOException | ClassNotFoundException e) {
            throw new FichierIntrouvableException("Erreur lors du chargement: " + e.getMessage());
        }
    }

    public static ArrayList<Module> lireModulesDepuisFichier(String nomFichier)
            throws FichierIntrouvableException, FormatFichierInvalidException {

        if (!nomFichier.endsWith(".txt"))
            throw new FormatFichierInvalidException("Format invalide, un fichier .txt est requis.");

        ArrayList<Module> modules = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(nomFichier))) {
            String ligne;
            int numeroLigne = 0;
            while ((ligne = br.readLine()) != null) {
                numeroLigne++;
                String[] parts = ligne.split(":");
                if (parts.length != 2)
                    throw new FormatFichierInvalidException(
                            "Ligne " + numeroLigne + " mal formatee: \"" + ligne + "\"");
                try {
                    String nom = parts[0].trim();
                    int credit = Integer.parseInt(parts[1].trim());
                    modules.add(new Module(nom, credit));
                } catch (NumberFormatException e) {
                    throw new FormatFichierInvalidException(
                            "Credit invalide a la ligne " + numeroLigne + ": \"" + ligne + "\"");
                }
            }
        } catch (FileNotFoundException e) {
            throw new FichierIntrouvableException("Fichier introuvable: " + nomFichier);
        } catch (IOException e) {
            throw new RuntimeException("Erreur de lecture: " + e.getMessage(), e);
        }

        return modules;
    }
}