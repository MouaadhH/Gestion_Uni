import java.io.*;
import java.util.ArrayList;

public class SaveLoad {

    public static void saveToFile(Section section)throws FichierIntrouvableException  {

        try {
            FileOutputStream fileOut = new FileOutputStream(section.nomSection+".dat");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(section);
            fileOut.close(); out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Section sauvegarder dans"+section.nomSection+".dat");
    }

    public static Section loadFromFile(String nomSection) {
        try {
            FileInputStream fileIn = new FileInputStream(nomSection + ".dat");
            ObjectInputStream in = new ObjectInputStream(fileIn);

            Section section = (Section) in.readObject();

            in.close();
            fileIn.close();

            System.out.println("Section chargée depuis " + nomSection + ".dat");

            return section;

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void lireModulesDepuisFichier(String nomFichier) throws FormatFichierInvalidException {
        ArrayList<Module> modules = new ArrayList<>();

        if(!nomFichier.contains("txt")) throw new FormatFichierInvalidException("Fichier mal formate");

        try (BufferedReader br = new BufferedReader(new FileReader("modules.txt"))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {

                String[] parts = ligne.split(":");
                if (parts.length == 2) {
                    String nom = parts[0].trim(); // nom du module
                    int credit = Integer.parseInt(parts[1].trim()); // crédit
                    modules.add(new Module(nom, credit));
                }
            }
        } catch (IOException e) {
            System.out.println("acces aux fichier echouee");
        }

        // Affichage pour vérifier
        for (Module m : modules) {
            System.out.println(m.getNom()+":"+m.getCredit());
        }
    }


}
