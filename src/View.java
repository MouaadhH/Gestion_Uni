import Exceptions.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.io.*;

/**
 * View.java — Swing GUI for GestionUniversitaire
 *
 * Replaces the console-based main loop with a fully graphical interface.
 * All logic still delegates to the existing model classes:
 *   Cycle, Section, Groupe, Etudiant, Module, Semestre, SaveLoad, …
 *
 * Usage:
 *   public static void main(String[] args) {
 *       SwingUtilities.invokeLater(View::new);
 *   }
 */
public class View extends JFrame {

    // ─── Palette ──────────────────────────────────────────────────────────
    private static final Color C_BG         = new Color(0xF4F6FB);
    private static final Color C_SIDEBAR    = new Color(0x1B2A4A);
    private static final Color C_SIDEBAR_HL = new Color(0x243556);
    private static final Color C_ACCENT     = new Color(0x3B7DD8);
    private static final Color C_ACCENT2    = new Color(0x28C6A0);
    private static final Color C_DANGER     = new Color(0xE05260);
    private static final Color C_WARN       = new Color(0xF0A500);
    private static final Color C_HEADER     = new Color(0x1B2A4A);
    private static final Color C_CARD       = Color.WHITE;
    private static final Color C_TEXT_DARK  = new Color(0x1B2A4A);
    private static final Color C_TEXT_LIGHT = new Color(0xF0F4FF);
    private static final Color C_MUTED      = new Color(0x7B8BAD);
    private static final Color C_BORDER     = new Color(0xDCE3F0);
    private static final Font  FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font  FONT_SECTION = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font  FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font  FONT_MONO    = new Font("Consolas",  Font.PLAIN, 12);

    // ─── Model State ──────────────────────────────────────────────────────
    private final HashMap<String, Cycle> cycles = new HashMap<>();
    private Cycle   currCycle   = null;
    private Section currSection = null;

    // ─── Navigation ───────────────────────────────────────────────────────
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel     cardPanel  = new JPanel(cardLayout);
    private JLabel statusLabel;

    // ─── Sidebar buttons (for highlight toggling) ─────────────────────────
    private final java.util.List<JButton> sidebarBtns = new ArrayList<>();
    private       JLabel lblCycleName, lblSectionName;

    // ─── Tab-content panels (created once, referenced for refresh) ────────
    private JTable studentTable;
    private DefaultTableModel studentModel;
    private JTable groupTable;
    private DefaultTableModel groupModel;

    // ══════════════════════════════════════════════════════════════════════
    public View() {
        setTitle("Gestion Universitaire");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        buildFrame();
        cardLayout.show(cardPanel, "CYCLE_SETUP");
        setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TOP-LEVEL FRAME
    // ══════════════════════════════════════════════════════════════════════
    private void buildFrame() {
        setLayout(new BorderLayout());

        // ── Global header bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_HEADER);
        header.setPreferredSize(new Dimension(0, 52));
        header.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel appTitle = new JLabel("  🎓  Gestion Universitaire");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        appTitle.setForeground(C_TEXT_LIGHT);
        header.add(appTitle, BorderLayout.WEST);

        // Cycle + Section badge
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        badge.setOpaque(false);
        lblCycleName   = makeBadgeLabel("—  Aucun cycle");
        lblSectionName = makeBadgeLabel("—  Aucune section");
        badge.add(lblCycleName);
        badge.add(lblSectionName);
        header.add(badge, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── Status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        statusBar.setBackground(new Color(0xE8ECF5));
        statusBar.setBorder(new MatteBorder(1, 0, 0, 0, C_BORDER));
        statusLabel = new JLabel("Prêt");
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setForeground(C_MUTED);
        statusBar.add(statusLabel);
        add(statusBar, BorderLayout.SOUTH);

        // ── Card container
        cardPanel.setBackground(C_BG);
        add(cardPanel, BorderLayout.CENTER);

        // Add all screens
        cardPanel.add(buildCycleSetupPanel(),   "CYCLE_SETUP");
        cardPanel.add(buildSectionSetupPanel(), "SECTION_SETUP");
        cardPanel.add(buildMainPanel(),         "MAIN");
    }

    private JLabel makeBadgeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SMALL);
        l.setForeground(new Color(0xA0B4D8));
        l.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        return l;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SCREEN 1 — CYCLE SETUP
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildCycleSetupPanel() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(C_BG);

        JPanel card = makeCard(480, -1);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Title
        JLabel title = new JLabel("Configuration du Cycle d'Études");
        title.setFont(FONT_TITLE);
        title.setForeground(C_TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(6));

        JLabel sub = new JLabel("Choisissez un type de cycle pour commencer");
        sub.setFont(FONT_BODY);
        sub.setForeground(C_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(sub);
        card.add(Box.createVerticalStrut(24));

        // Cycle buttons grid
        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.setOpaque(false);
        String[][] cycleInfo = {
                {"Ingénieur","5 ans · 10 semestres","1"},
                {"Licence",  "3 ans · 6 semestres", "2"},
                {"Master",   "2 ans · 4 semestres", "3"},
                {"Doctorat", "3 ans · 6 semestres", "4"}
        };
        ButtonGroup cycleGroup = new ButtonGroup();
        JToggleButton[] cycleBtns = new JToggleButton[4];
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            JToggleButton btn = makeCycleToggle(cycleInfo[i][0], cycleInfo[i][1]);
            cycleGroup.add(btn);
            cycleBtns[i] = btn;
            grid.add(btn);
        }
        card.add(grid);
        card.add(Box.createVerticalStrut(20));

        // Spécialité
        JLabel lblSpec = new JLabel("Spécialité *");
        lblSpec.setFont(FONT_SECTION);
        lblSpec.setForeground(C_TEXT_DARK);
        lblSpec.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblSpec);
        card.add(Box.createVerticalStrut(4));

        JTextField tfSpecialite = makeTextField("Ex: Informatique, Génie Civil…");
        tfSpecialite.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(tfSpecialite);
        card.add(Box.createVerticalStrut(20));

        // Existing cycle section
        JSeparator sep = new JSeparator();
        sep.setForeground(C_BORDER);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(sep);
        card.add(Box.createVerticalStrut(12));

        JLabel lblExist = new JLabel("— Ou gérer un cycle existant —");
        lblExist.setFont(FONT_SMALL);
        lblExist.setForeground(C_MUTED);
        lblExist.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblExist);
        card.add(Box.createVerticalStrut(8));

        JPanel existRow = new JPanel(new BorderLayout(8, 0));
        existRow.setOpaque(false);
        JTextField tfExistNom = makeTextField("Nom du cycle existant");
        JButton btnGerer = makeButton("Gérer", C_ACCENT2);
        existRow.add(tfExistNom, BorderLayout.CENTER);
        existRow.add(btnGerer,   BorderLayout.EAST);
        existRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(existRow);
        card.add(Box.createVerticalStrut(24));

        // Create button
        JButton btnCreate = makeButton("Créer le Cycle  →", C_ACCENT);
        btnCreate.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCreate.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        card.add(btnCreate);

        // ── Actions
        btnCreate.addActionListener(e -> {
            // Determine selected cycle type
            int sel = -1;
            for (int i = 0; i < 4; i++) if (cycleBtns[i].isSelected()) { sel = i; break; }
            if (sel == -1) { showError("Veuillez sélectionner un type de cycle."); return; }

            String spec = tfSpecialite.getText().trim();
            if (spec.isEmpty()) { showError("La spécialité est obligatoire."); return; }

            int[] durees   = {5, 3, 2, 3};
            int[] semestres = {10, 6, 4, 6};
            String[] noms  = {"Ingenieur", "Licence", "Master", "Doctorat"};

            currCycle = new Cycle(noms[sel], durees[sel], semestres[sel]);
            currCycle.specialite = spec;
            cycles.put(currCycle.nomCompose, currCycle);

            lblCycleName.setText("  📚  " + currCycle.nomCompose + " — " + spec);
            setStatus("Cycle créé : " + currCycle.nomCompose + " en " + spec);
            cardLayout.show(cardPanel, "SECTION_SETUP");
        });

        btnGerer.addActionListener(e -> {
            String nom = tfExistNom.getText().trim();
            if (nom.isEmpty()) { showError("Entrez le nom du cycle."); return; }
            Cycle found = cycles.get(nom);
            if (found == null) { showError("Cycle introuvable : " + nom); return; }
            currCycle = found;
            lblCycleName.setText("  📚  " + currCycle.nomCompose + " — " + currCycle.specialite);
            setStatus("Cycle chargé : " + currCycle.nomCompose);
            cardLayout.show(cardPanel, "SECTION_SETUP");
        });

        root.add(card);
        return root;
    }

    private JToggleButton makeCycleToggle(String name, String detail) {
        JToggleButton btn = new JToggleButton("<html><b>" + name + "</b><br><small>" + detail + "</small></html>");
        btn.setFont(FONT_BODY);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(C_CARD);
        btn.setForeground(C_TEXT_DARK);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(C_BORDER, 2, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        btn.setPreferredSize(new Dimension(200, 64));

        btn.addChangeListener(ev -> {
            if (btn.isSelected()) {
                btn.setBackground(new Color(0xEBF2FF));
                btn.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(C_ACCENT, 2, true),
                        BorderFactory.createEmptyBorder(12, 16, 12, 16)
                ));
            } else {
                btn.setBackground(C_CARD);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(C_BORDER, 2, true),
                        BorderFactory.createEmptyBorder(12, 16, 12, 16)
                ));
            }
        });
        return btn;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SCREEN 2 — SECTION SETUP
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildSectionSetupPanel() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(C_BG);

        JPanel card = makeCard(460, -1);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Configuration de la Section");
        title.setFont(FONT_TITLE);
        title.setForeground(C_TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(20));

        // Nom section
        card.add(makeFieldLabel("Nom de la section *"));
        JTextField tfNom = makeTextField("Ex: Section A");
        tfNom.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(tfNom);
        card.add(Box.createVerticalStrut(14));

        // Niveau
        card.add(makeFieldLabel("Niveau d'étude *"));
        JSpinner spNiveau = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
        spNiveau.setFont(FONT_BODY);
        spNiveau.setAlignmentX(Component.LEFT_ALIGNMENT);
        spNiveau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        card.add(spNiveau);
        card.add(Box.createVerticalStrut(24));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        JButton btnBack   = makeButton("← Retour",      C_MUTED);
        JButton btnCreate = makeButton("Créer la Section  →", C_ACCENT);
        btnRow.add(btnBack);
        btnRow.add(btnCreate);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(btnRow);

        // Gérer section existante
        card.add(Box.createVerticalStrut(16));
        JSeparator sep = new JSeparator(); sep.setForeground(C_BORDER);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT); card.add(sep);
        card.add(Box.createVerticalStrut(10));
        JLabel lblE = new JLabel("— Gérer une section existante —");
        lblE.setFont(FONT_SMALL); lblE.setForeground(C_MUTED);
        lblE.setAlignmentX(Component.CENTER_ALIGNMENT); card.add(lblE);
        card.add(Box.createVerticalStrut(8));
        JPanel existRow = new JPanel(new BorderLayout(8, 0));
        existRow.setOpaque(false);
        JTextField tfExist = makeTextField("Nom de la section");
        JButton btnGerer   = makeButton("Gérer", C_ACCENT2);
        existRow.add(tfExist, BorderLayout.CENTER);
        existRow.add(btnGerer, BorderLayout.EAST);
        existRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(existRow);

        // ── Actions
        btnBack.addActionListener(e -> cardLayout.show(cardPanel, "CYCLE_SETUP"));

        btnCreate.addActionListener(e -> {
            String nom = tfNom.getText().trim();
            if (nom.isEmpty()) { showError("Le nom est obligatoire."); return; }
            int niv = (Integer) spNiveau.getValue();
            if (currCycle == null) { showError("Aucun cycle sélectionné."); return; }
            if (niv > currCycle.duree) {
                showError("Niveau max pour ce cycle : " + currCycle.duree);
                return;
            }
            currSection = new Section(nom, currCycle.nomCompose + currCycle.specialite, niv);
            currCycle.sections.add(currSection);
            lblSectionName.setText("  📋  " + nom + " — Année " + niv);
            setStatus("Section créée : " + nom);
            refreshStudentTable();
            cardLayout.show(cardPanel, "MAIN");
        });

        btnGerer.addActionListener(e -> {
            if (currCycle == null) { showError("Aucun cycle chargé."); return; }
            String nom = tfExist.getText().trim();
            if (nom.isEmpty()) { showError("Entrez le nom."); return; }
            Section found = null;
            for (Section s : currCycle.sections) {
                if (s.nomSection.equals(nom)) { found = s; break; }
            }
            if (found == null) { showError("Section introuvable."); return; }
            currSection = found;
            lblSectionName.setText("  📋  " + found.nomSection + " — Année " + found.niveau);
            setStatus("Section chargée : " + found.nomSection);
            refreshStudentTable();
            cardLayout.show(cardPanel, "MAIN");
        });

        root.add(card);
        return root;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SCREEN 3 — MAIN PANEL (sidebar + tabs)
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildMainPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_BG);

        // ── Sidebar
        JPanel sidebar = buildSidebar();
        root.add(sidebar, BorderLayout.WEST);

        // ── Content area (CardLayout driven by sidebar)
        CardLayout contentCL = new CardLayout();
        JPanel contentArea = new JPanel(contentCL);
        contentArea.setBackground(C_BG);

        contentArea.add(buildStudentPanel(),   "STUDENTS");
        contentArea.add(buildGroupPanel(),     "GROUPS");
        contentArea.add(buildModulePanel(),    "MODULES");
        contentArea.add(buildSemestrePanel(),  "SEMESTRES");
        contentArea.add(buildAssignPanel(),    "ASSIGN");
        contentArea.add(buildStatsPanel(),     "STATS");
        contentArea.add(buildSaveLoadPanel(),  "SAVELOAD");

        root.add(contentArea, BorderLayout.CENTER);

        // Wire sidebar buttons
        String[] screens = {"STUDENTS","GROUPS","MODULES","SEMESTRES","ASSIGN","STATS","SAVELOAD"};
        for (int i = 0; i < sidebarBtns.size(); i++) {
            final String screen = screens[i];
            final JButton btn = sidebarBtns.get(i);
            btn.addActionListener(e -> {
                sidebarBtns.forEach(b -> b.setBackground(C_SIDEBAR));
                btn.setBackground(C_SIDEBAR_HL);
                contentCL.show(contentArea, screen);
            });
        }

        // Default highlight
        if (!sidebarBtns.isEmpty()) {
            sidebarBtns.get(0).setBackground(C_SIDEBAR_HL);
        }

        return root;
    }

    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(C_SIDEBAR);
        sb.setPreferredSize(new Dimension(210, 0));
        sb.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));

        JLabel nav = new JLabel("  NAVIGATION");
        nav.setFont(new Font("Segoe UI", Font.BOLD, 10));
        nav.setForeground(C_MUTED);
        nav.setAlignmentX(Component.LEFT_ALIGNMENT);
        sb.add(nav);
        sb.add(Box.createVerticalStrut(10));

        String[][] items = {
                {"👨‍🎓  Étudiants",   "Gérer les étudiants"},
                {"👥  Groupes",      "Gérer les groupes"},
                {"📖  Modules",      "Modules et notes"},
                {"📅  Semestres",    "Gestion semestres"},
                {"📂  Affecter",     "Importer modules"},
                {"📊  Statistiques", "Vue d'ensemble"},
                {"💾  Sauvegarde",   "Sauvegarder / Charger"},
        };

        for (String[] item : items) {
            JButton btn = new JButton(item[0]);
            btn.setFont(FONT_BODY);
            btn.setForeground(C_TEXT_LIGHT);
            btn.setBackground(C_SIDEBAR);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 12));
            btn.setToolTipText(item[1]);
            sidebarBtns.add(btn);
            sb.add(btn);
        }

        sb.add(Box.createVerticalGlue());

        // Back to cycle setup
        JButton btnBack = new JButton("⚙  Changer de Cycle");
        btnBack.setFont(FONT_SMALL);
        btnBack.setForeground(C_MUTED);
        btnBack.setBackground(C_SIDEBAR);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.setHorizontalAlignment(SwingConstants.LEFT);
        btnBack.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnBack.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 12));
        btnBack.addActionListener(e -> cardLayout.show(cardPanel, "CYCLE_SETUP"));
        sb.add(btnBack);

        return sb;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TAB: STUDENTS
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildStudentPanel() {
        JPanel root = makeContentRoot("Gestion des Étudiants");

        // ── Add student form (card)
        JPanel addCard = makeCard(-1, -1);
        addCard.setLayout(new BoxLayout(addCard, BoxLayout.Y_AXIS));
        sectionTitle(addCard, "➕  Inscrire un Étudiant");

        // Row 1: ID / Nom
        JPanel r1 = makeFormRow();
        JTextField tfId    = makeTextField("ID étudiant");
        JTextField tfNom   = makeTextField("Nom");
        r1.add(labeledField("ID *", tfId));
        r1.add(labeledField("Nom *", tfNom));
        addCard.add(r1);
        addCard.add(Box.createVerticalStrut(10));

        // Row 2: Prénom / Email
        JPanel r2 = makeFormRow();
        JTextField tfPrenom = makeTextField("Prénom");
        JTextField tfEmail  = makeTextField("Email");
        r2.add(labeledField("Prénom *", tfPrenom));
        r2.add(labeledField("Email *", tfEmail));
        addCard.add(r2);
        addCard.add(Box.createVerticalStrut(10));

        // Row 3: Date naissance / Groupe
        JPanel r3 = makeFormRow();
        JTextField tfDate   = makeTextField("AAAA-MM-JJ");
        JTextField tfGroupe = makeTextField("Nom du groupe");
        r3.add(labeledField("Date de naissance *", tfDate));
        r3.add(labeledField("Groupe *", tfGroupe));
        addCard.add(r3);
        addCard.add(Box.createVerticalStrut(14));

        JButton btnAdd = makeButton("Inscrire l'Étudiant", C_ACCENT);
        btnAdd.setAlignmentX(Component.LEFT_ALIGNMENT);
        addCard.add(btnAdd);

        btnAdd.addActionListener(e -> {
            if (currSection == null) { showError("Aucune section active."); return; }
            String id = tfId.getText().trim(), nom = tfNom.getText().trim(),
                    prenom = tfPrenom.getText().trim(), email = tfEmail.getText().trim(),
                    dateStr = tfDate.getText().trim(), grp = tfGroupe.getText().trim();
            if (id.isEmpty()||nom.isEmpty()||prenom.isEmpty()||email.isEmpty()||dateStr.isEmpty()||grp.isEmpty()) {
                showError("Tous les champs sont obligatoires."); return;
            }
            LocalDate dob;
            try { dob = LocalDate.parse(dateStr); }
            catch (DateTimeParseException ex) { showError("Format de date invalide (AAAA-MM-JJ)."); return; }

            // Auto-create group if needed
            if (!currSection.groupes.containsKey(grp)) {
                currSection.ajouterGroupe(new Groupe(grp));
            }
            if (currSection.groupes.get(grp).etudiants.size() >= 30) {
                showError("Ce groupe est plein (max 30)."); return;
            }
            Etudiant etu = null;
            try {
                etu = new Etudiant(id, nom, prenom, email, dob);
            } catch (DateNaissanceInvalideException ex) {
                throw new RuntimeException(ex);
            } catch (EmailInvalidException ex) {
                throw new RuntimeException(ex);
            }
            try {
                currSection.ajouterEtudiantDansGroupe(etu, grp);
            } catch (GroupePleinException ex) {
                throw new RuntimeException(ex);
            }
            refreshStudentTable();
            clearFields(tfId, tfNom, tfPrenom, tfEmail, tfDate, tfGroupe);
            setStatus("Étudiant inscrit : " + nom + " " + prenom);
            showSuccess("Inscription réussie !");
        });

        // ── Table + actions
        JPanel tableCard = makeCard(-1, -1);
        tableCard.setLayout(new BorderLayout(0, 12));
        sectionTitleBorder(tableCard, "📋  Liste des Étudiants", BorderLayout.NORTH);

        String[] cols = {"ID","Nom","Prénom","Email","Date Naissance","Groupe","Modules"};
        studentModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        studentTable = new JTable(studentModel);
        styleTable(studentTable);
        tableCard.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        // Delete / Search bar
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionRow.setOpaque(false);
        JTextField tfSearch = makeTextField("Rechercher par ID ou nom…");
        tfSearch.setPreferredSize(new Dimension(240, 32));
        JButton btnSearch = makeButton("Rechercher", C_ACCENT2);
        JButton btnDelete = makeButton("Supprimer", C_DANGER);
        JButton btnRefresh = makeButton("Rafraîchir", C_MUTED);
        actionRow.add(tfSearch);
        actionRow.add(btnSearch);
        actionRow.add(btnDelete);
        actionRow.add(btnRefresh);
        tableCard.add(actionRow, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> refreshStudentTable());

        btnSearch.addActionListener(e -> {
            if (currSection == null) return;
            String q = tfSearch.getText().trim();
            if (q.isEmpty()) { refreshStudentTable(); return; }
            studentModel.setRowCount(0);
            for (Groupe g : currSection.groupes.values()) {
                for (Etudiant et : g.etudiants) {
                    if (et.getId().equalsIgnoreCase(q) || et.getNom().toLowerCase().contains(q.toLowerCase())) {
                        studentModel.addRow(new Object[]{
                                et.getId(), et.getNom(), et.getPrenom(), et.getEmail(),
                                et.getDateNaissance(), g.nomGroupe, et.modules.size()
                        });
                    }
                }
            }
        });

        btnDelete.addActionListener(e -> {
            if (currSection == null) return;
            int row = studentTable.getSelectedRow();
            if (row < 0) { showError("Sélectionnez un étudiant dans la liste."); return; }
            String idDel  = (String) studentModel.getValueAt(row, 0);
            String grpDel = (String) studentModel.getValueAt(row, 5);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Supprimer l'étudiant " + idDel + " ?", "Confirmation",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    currSection.supprimerEtudiantDeGroupe(idDel, grpDel);
                } catch (EtudiantNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
                refreshStudentTable();
                setStatus("Étudiant supprimé : " + idDel);
            }
        });

        root.add(addCard,   "add");
        root.add(tableCard, "table");
        return root;
    }

    private void refreshStudentTable() {
        if (studentModel == null || currSection == null) return;
        studentModel.setRowCount(0);
        for (Groupe g : currSection.groupes.values()) {
            for (Etudiant et : g.etudiants) {
                studentModel.addRow(new Object[]{
                        et.getId(), et.getNom(), et.getPrenom(), et.getEmail(),
                        et.getDateNaissance(), g.nomGroupe, et.modules.size()
                });
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TAB: GROUPS
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildGroupPanel() {
        JPanel root = makeContentRoot("Gestion des Groupes");

        // ── Create group
        JPanel createCard = makeCard(-1, -1);
        createCard.setLayout(new BoxLayout(createCard, BoxLayout.Y_AXIS));
        sectionTitle(createCard, "➕  Créer un Groupe");

        JTextField tfNomG = makeTextField("Nom du groupe");
        tfNomG.setAlignmentX(Component.LEFT_ALIGNMENT);
        createCard.add(makeFieldLabel("Nom *"));
        createCard.add(tfNomG);
        createCard.add(Box.createVerticalStrut(12));
        JButton btnCreateG = makeButton("Créer le Groupe", C_ACCENT);
        btnCreateG.setAlignmentX(Component.LEFT_ALIGNMENT);
        createCard.add(btnCreateG);

        btnCreateG.addActionListener(e -> {
            if (currSection == null) { showError("Aucune section active."); return; }
            String nom = tfNomG.getText().trim();
            if (nom.isEmpty()) { showError("Le nom est obligatoire."); return; }
            currSection.ajouterGroupe(new Groupe(nom));
            refreshGroupTable();
            tfNomG.setText("");
            setStatus("Groupe créé : " + nom);
            showSuccess("Groupe créé avec succès !");
        });

        // ── Move student
        JPanel moveCard = makeCard(-1, -1);
        moveCard.setLayout(new BoxLayout(moveCard, BoxLayout.Y_AXIS));
        sectionTitle(moveCard, "🔀  Déplacer un Étudiant");

        JPanel mRow = makeFormRow();
        JTextField tfMoveId    = makeTextField("ID étudiant");
        JTextField tfOldGroup  = makeTextField("Ancien groupe");
        JTextField tfNewGroup  = makeTextField("Nouveau groupe");
        mRow.add(labeledField("ID *",           tfMoveId));
        mRow.add(labeledField("Ancien groupe *", tfOldGroup));
        mRow.add(labeledField("Nouveau groupe *",tfNewGroup));
        moveCard.add(mRow);
        moveCard.add(Box.createVerticalStrut(12));
        JButton btnMove = makeButton("Déplacer", C_WARN);
        btnMove.setAlignmentX(Component.LEFT_ALIGNMENT);
        moveCard.add(btnMove);

        btnMove.addActionListener(e -> {
            if (currSection == null) { showError("Aucune section active."); return; }
            String id = tfMoveId.getText().trim(),
                    old = tfOldGroup.getText().trim(),
                    newG = tfNewGroup.getText().trim();
            if (id.isEmpty()||old.isEmpty()||newG.isEmpty()) {
                showError("Tous les champs sont obligatoires."); return;
            }
            try {
                currSection.changerEtudiantDeGroupe(id, old, newG);
                refreshStudentTable();
                refreshGroupTable();
                clearFields(tfMoveId, tfOldGroup, tfNewGroup);
                setStatus("Étudiant " + id + " déplacé vers " + newG);
                showSuccess("Déplacement effectué !");
            } catch (Exception ex) {
                showError("Erreur : " + ex.getMessage());
            }
        });

        // ── Delete group
        JPanel delCard = makeCard(-1, -1);
        delCard.setLayout(new BoxLayout(delCard, BoxLayout.Y_AXIS));
        sectionTitle(delCard, "🗑  Supprimer un Groupe");

        JTextField tfDelG = makeTextField("Nom du groupe");
        tfDelG.setAlignmentX(Component.LEFT_ALIGNMENT);
        delCard.add(makeFieldLabel("Nom *"));
        delCard.add(tfDelG);
        delCard.add(Box.createVerticalStrut(12));
        JButton btnDelG = makeButton("Supprimer le Groupe", C_DANGER);
        btnDelG.setAlignmentX(Component.LEFT_ALIGNMENT);
        delCard.add(btnDelG);

        btnDelG.addActionListener(e -> {
            if (currSection == null) { showError("Aucune section active."); return; }
            String nom = tfDelG.getText().trim();
            if (nom.isEmpty()) { showError("Le nom est obligatoire."); return; }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Supprimer le groupe " + nom + " et tous ses étudiants ?",
                    "Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    currSection.supprimerGroupe(nom);
                } catch (GroupePleinException ex) {
                    throw new RuntimeException(ex);
                }
                refreshGroupTable();
                refreshStudentTable();
                tfDelG.setText("");
                setStatus("Groupe supprimé : " + nom);
            }
        });

        // ── Group overview table
        JPanel tableCard = makeCard(-1, -1);
        tableCard.setLayout(new BorderLayout(0, 10));
        sectionTitleBorder(tableCard, "📋  Aperçu des Groupes", BorderLayout.NORTH);

        String[] cols = {"Groupe", "Étudiants", "Capacité restante", "Moyenne"};
        groupModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        groupTable = new JTable(groupModel);
        styleTable(groupTable);
        tableCard.add(new JScrollPane(groupTable), BorderLayout.CENTER);

        JButton btnRefG = makeButton("Rafraîchir", C_MUTED);
        btnRefG.addActionListener(ev -> refreshGroupTable());
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT)); bar.setOpaque(false);
        bar.add(btnRefG);
        tableCard.add(bar, BorderLayout.SOUTH);

        root.add(createCard, "cg");
        root.add(moveCard,   "mv");
        root.add(delCard,    "dg");
        root.add(tableCard,  "gt");
        return root;
    }

    private void refreshGroupTable() {
        if (groupModel == null || currSection == null) return;
        groupModel.setRowCount(0);
        for (Groupe g : currSection.groupes.values()) {
            double moy = g.calculerMoyenneGeneraldeGroupe();
            groupModel.addRow(new Object[]{
                    g.nomGroupe, g.etudiants.size(), 30 - g.etudiants.size(),
                    String.format("%.2f / 20", moy)
            });
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TAB: MODULES & NOTES
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildModulePanel() {
        JPanel root = makeContentRoot("Modules et Notes");

        // ── Add module to student
        JPanel modCard = makeCard(-1, -1);
        modCard.setLayout(new BoxLayout(modCard, BoxLayout.Y_AXIS));
        sectionTitle(modCard, "➕  Ajouter un Module à un Étudiant");
        JPanel r1 = makeFormRow();
        JTextField tfModId  = makeTextField("ID étudiant");
        JTextField tfModNom = makeTextField("Nom du module");
        JSpinner   spCredit = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        spCredit.setFont(FONT_BODY);
        r1.add(labeledField("ID étudiant *", tfModId));
        r1.add(labeledField("Nom module *",  tfModNom));
        r1.add(labeledField("Crédits",       spCredit));
        modCard.add(r1); modCard.add(Box.createVerticalStrut(12));
        JButton btnAddMod = makeButton("Ajouter le Module", C_ACCENT);
        btnAddMod.setAlignmentX(Component.LEFT_ALIGNMENT);
        modCard.add(btnAddMod);

        btnAddMod.addActionListener(e -> {
            if (currSection == null) { showError("Aucune section active."); return; }
            String id = tfModId.getText().trim(), nom = tfModNom.getText().trim();
            if (id.isEmpty()||nom.isEmpty()) { showError("Champs obligatoires manquants."); return; }
            Etudiant etu = null;
            try {
                etu = currSection.getEtudiant(id);
            } catch (EtudiantNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            if (etu == null) { showError("Étudiant introuvable."); return; }
            etu.ajouterModule(new Module(nom, (Integer) spCredit.getValue()));
            clearFields(tfModId, tfModNom);
            setStatus("Module ajouté à l'étudiant " + id);
            showSuccess("Module ajouté !");
        });

        // ── Enter grade
        JPanel noteCard = makeCard(-1, -1);
        noteCard.setLayout(new BoxLayout(noteCard, BoxLayout.Y_AXIS));
        sectionTitle(noteCard, "📝  Saisir une Note");
        JPanel nr = makeFormRow();
        JTextField tfNoteId  = makeTextField("ID étudiant");
        JTextField tfNoteMod = makeTextField("Nom du module");
        String[]   typeNames = {"Test", "Examen", "TP"};
        JComboBox<String> cbType = new JComboBox<>(typeNames);
        cbType.setFont(FONT_BODY);
        JSpinner spNote = new JSpinner(new SpinnerNumberModel(10.0, 0.0, 20.0, 0.5));
        spNote.setFont(FONT_BODY);
        nr.add(labeledField("ID étudiant *", tfNoteId));
        nr.add(labeledField("Module *",      tfNoteMod));
        nr.add(labeledField("Type",          cbType));
        nr.add(labeledField("Note (0-20)",   spNote));
        noteCard.add(nr); noteCard.add(Box.createVerticalStrut(12));
        JButton btnNote = makeButton("Enregistrer la Note", C_ACCENT2);
        btnNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        noteCard.add(btnNote);

        btnNote.addActionListener(e -> {
            if (currSection == null) { showError("Aucune section active."); return; }
            String id = tfNoteId.getText().trim(), mod = tfNoteMod.getText().trim();
            if (id.isEmpty()||mod.isEmpty()) { showError("Champs obligatoires manquants."); return; }
            Etudiant etu = null;
            try {
                etu = currSection.getEtudiant(id);
            } catch (EtudiantNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            if (etu == null) { showError("Étudiant introuvable."); return; }
            Module m = null;
            try {
                m = etu.getModule(mod);
            } catch (ModuleNotFound ex) {
                throw new RuntimeException(ex);
            }
            if (m == null) { showError("Module introuvable."); return; }
            double val = (Double) spNote.getValue();
            int type = cbType.getSelectedIndex();
            if (type == 0) {
                try {
                    m.setTest(val);
                } catch (NoteInvalidException ex) {
                    throw new RuntimeException(ex);
                }
            }
            else if (type == 1) {
                try {
                    m.setExam(val);
                } catch (NoteInvalidException ex) {
                    throw new RuntimeException(ex);
                }
            }
            else {
                try {
                    m.setTp(val);
                } catch (NoteInvalidException ex) {
                    throw new RuntimeException(ex);
                }
            }
            clearFields(tfNoteId, tfNoteMod);
            setStatus("Note enregistrée");
            showSuccess("Note enregistrée !");
        });

        // ── Transcript viewer
        JPanel transcCard = makeCard(-1, -1);
        transcCard.setLayout(new BorderLayout(0, 10));
        JPanel tcTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tcTop.setOpaque(false);
        JLabel tlbl = new JLabel("📄  Relevé de Notes — ID:");
        tlbl.setFont(FONT_SECTION); tlbl.setForeground(C_TEXT_DARK);
        JTextField tfRId = makeTextField("ID étudiant");
        tfRId.setPreferredSize(new Dimension(180, 32));
        JButton btnReleve = makeButton("Afficher", C_ACCENT);
        tcTop.add(tlbl); tcTop.add(tfRId); tcTop.add(btnReleve);
        transcCard.add(tcTop, BorderLayout.NORTH);

        JTextArea taReleve = new JTextArea();
        taReleve.setFont(FONT_MONO);
        taReleve.setEditable(false);
        taReleve.setBackground(new Color(0xF8FAFF));
        taReleve.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        transcCard.add(new JScrollPane(taReleve), BorderLayout.CENTER);

        btnReleve.addActionListener(e -> {
            if (currSection == null) { showError("Aucune section active."); return; }
            String id = tfRId.getText().trim();
            if (id.isEmpty()) { showError("Entrez l'ID."); return; }
            Etudiant etu = null;
            try {
                etu = currSection.getEtudiant(id);
            } catch (EtudiantNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            if (etu == null) { showError("Étudiant introuvable."); return; }
            StringBuilder sb = new StringBuilder();
            sb.append("══════════════════════════════════════════\n");
            sb.append("  RELEVÉ DE NOTES\n");
            sb.append("══════════════════════════════════════════\n");
            sb.append("  Étudiant : ").append(etu.getNom()).append(" ").append(etu.getPrenom()).append("\n");
            sb.append("  ID       : ").append(etu.getId()).append("\n");
            sb.append("  Email    : ").append(etu.getEmail()).append("\n");
            sb.append("══════════════════════════════════════════\n");
            for (Module m : etu.modules) {
                sb.append("  ").append(m.getNom()).append(" (").append(m.getCredit()).append(" cr)\n");
                sb.append("    Test   : ").append(m.getTest()  == null ? "---" : m.getTest()).append("\n");
                sb.append("    TP     : ").append(m.getTp()    == null ? "---" : m.getTp()).append("\n");
                sb.append("    Examen : ").append(m.getExam()  == null ? "---" : m.getExam()).append("\n");
                sb.append(String.format("    Moyenne: %.2f / 20\n", m.calculerMoyenneModule()));
                sb.append("  ──────────────────────────────────────\n");
            }
            sb.append(String.format("  MOYENNE GÉNÉRALE : %.2f / 20\n", etu.calculerMoyenneGenerale()));
            sb.append("══════════════════════════════════════════\n");
            taReleve.setText(sb.toString());
        });

        root.add(modCard,   "mc");
        root.add(noteCard,  "nc");
        root.add(transcCard,"tc");
        return root;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TAB: SEMESTRES
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildSemestrePanel() {
        JPanel root = makeContentRoot("Gestion des Semestres");

        // ── Create semester
        JPanel semCard = makeCard(-1, -1);
        semCard.setLayout(new BoxLayout(semCard, BoxLayout.Y_AXIS));
        sectionTitle(semCard, "➕  Créer un Semestre");
        JSpinner spSemNum = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        spSemNum.setFont(FONT_BODY);
        spSemNum.setAlignmentX(Component.LEFT_ALIGNMENT);
        semCard.add(makeFieldLabel("Numéro du semestre *"));
        semCard.add(spSemNum); semCard.add(Box.createVerticalStrut(12));
        JButton btnCreateSem = makeButton("Créer le Semestre", C_ACCENT);
        btnCreateSem.setAlignmentX(Component.LEFT_ALIGNMENT);
        semCard.add(btnCreateSem);

        btnCreateSem.addActionListener(e -> {
            if (currSection == null) { showError("Aucune section active."); return; }
            int num = (Integer) spSemNum.getValue();
            if (currSection.semesters.containsKey(num)) {
                showError("Le semestre " + num + " existe déjà."); return;
            }
            currSection.semesters.put(num, new Semestre(num));
            refreshSemTable(null, null);
            setStatus("Semestre " + num + " créé.");
            showSuccess("Semestre créé !");
        });

        // ── Add / remove module in semester
        JPanel modSemCard = makeCard(-1, -1);
        modSemCard.setLayout(new BoxLayout(modSemCard, BoxLayout.Y_AXIS));
        sectionTitle(modSemCard, "📖  Ajouter / Retirer un Module");
        JPanel mr = makeFormRow();
        JSpinner spSemMod = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        spSemMod.setFont(FONT_BODY);
        JTextField tfSemModNom = makeTextField("Nom du module");
        mr.add(labeledField("Semestre *",      spSemMod));
        mr.add(labeledField("Nom du module *", tfSemModNom));
        modSemCard.add(mr); modSemCard.add(Box.createVerticalStrut(12));
        JPanel btRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btRow2.setOpaque(false);
        JButton btnAddSMod = makeButton("Ajouter", C_ACCENT2);
        JButton btnDelSMod = makeButton("Retirer",  C_DANGER);
        btRow2.add(btnAddSMod); btRow2.add(btnDelSMod);
        modSemCard.add(btRow2);

        // ── Semester overview table (shared TextArea)
        JPanel viewCard = makeCard(-1, -1);
        viewCard.setLayout(new BorderLayout(0, 10));
        JLabel viewTitle = new JLabel("📋  Semestres & Modules");
        viewTitle.setFont(FONT_SECTION); viewTitle.setForeground(C_TEXT_DARK);
        JButton btnRefS = makeButton("Rafraîchir", C_MUTED);
        JPanel vh = new JPanel(new BorderLayout()); vh.setOpaque(false);
        vh.add(viewTitle, BorderLayout.WEST); vh.add(btnRefS, BorderLayout.EAST);
        viewCard.add(vh, BorderLayout.NORTH);

        JTextArea taSem = new JTextArea();
        taSem.setFont(FONT_MONO); taSem.setEditable(false);
        taSem.setBackground(new Color(0xF8FAFF));
        taSem.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        viewCard.add(new JScrollPane(taSem), BorderLayout.CENTER);

        // Wire refresh
        Runnable refresh = () -> {
            if (currSection == null) return;
            StringBuilder sb = new StringBuilder();
            for (Semestre s : currSection.semesters.values()) {
                sb.append("Semestre ").append(s.numero).append(" — ")
                        .append(s.nomsModules.size()).append(" module(s)\n");
                for (String mn : s.nomsModules) sb.append("   • ").append(mn).append("\n");
                sb.append("\n");
            }
            taSem.setText(sb.length() == 0 ? "Aucun semestre créé." : sb.toString());
        };

        btnRefS.addActionListener(e -> refresh.run());

        btnAddSMod.addActionListener(e -> {
            if (currSection == null) { showError("Aucune section active."); return; }
            int num = (Integer) spSemMod.getValue();
            String nom = tfSemModNom.getText().trim();
            if (nom.isEmpty()) { showError("Le nom du module est obligatoire."); return; }
            Semestre sem = currSection.semesters.get(num);
            if (sem == null) { showError("Semestre " + num + " introuvable. Créez-le d'abord."); return; }
            sem.ajouterNomModules(nom);
            tfSemModNom.setText("");
            refresh.run();
            setStatus("Module '" + nom + "' ajouté au semestre " + num);
        });

        btnDelSMod.addActionListener(e -> {
            if (currSection == null) { showError("Aucune section active."); return; }
            int num = (Integer) spSemMod.getValue();
            String nom = tfSemModNom.getText().trim();
            if (nom.isEmpty()) { showError("Le nom du module est obligatoire."); return; }
            Semestre sem = currSection.semesters.get(num);
            if (sem == null) { showError("Semestre introuvable."); return; }
            sem.nomsModules.remove(nom);
            tfSemModNom.setText("");
            refresh.run();
            setStatus("Module retiré.");
        });

        root.add(semCard,    "sc");
        root.add(modSemCard, "ms");
        root.add(viewCard,   "sv");
        return root;
    }

    private void refreshSemTable(JTable t, DefaultTableModel m) {
        // placeholder — semester uses TextArea instead
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TAB: ASSIGN MODULES FROM FILE
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildAssignPanel() {
        JPanel root = makeContentRoot("Affecter des Modules depuis Fichier");

        JPanel card = makeCard(600, -1);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        sectionTitle(card, "📂  Importer et Affecter des Modules");

        JLabel infoLbl = new JLabel("<html>Le fichier doit être un <b>.txt</b> avec un module par ligne<br>"
                + "au format : <code>NomModule;Crédits</code></html>");
        infoLbl.setFont(FONT_SMALL);
        infoLbl.setForeground(C_MUTED);
        infoLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(infoLbl);
        card.add(Box.createVerticalStrut(16));

        JPanel fr = makeFormRow();
        JTextField tfFich   = makeTextField("Nom du fichier (.txt)");
        JSpinner spSemAssign = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        spSemAssign.setFont(FONT_BODY);
        fr.add(labeledField("Fichier *",      tfFich));
        fr.add(labeledField("Numéro semestre *", spSemAssign));
        card.add(fr); card.add(Box.createVerticalStrut(16));

        JButton btnImport = makeButton("Importer & Affecter", C_ACCENT);
        btnImport.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(btnImport);
        card.add(Box.createVerticalStrut(16));

        JTextArea taLog = new JTextArea(8, 50);
        taLog.setFont(FONT_MONO); taLog.setEditable(false);
        taLog.setBackground(new Color(0xF0F4FF));
        taLog.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JScrollPane sp = new JScrollPane(taLog);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(sp);

        btnImport.addActionListener(e -> {
            if (currSection == null) { showError("Aucune section active."); return; }
            String fich = tfFich.getText().trim();
            if (fich.isEmpty()) { showError("Entrez le nom du fichier."); return; }
            int numSem = (Integer) spSemAssign.getValue();

            java.util.ArrayList<Module> modules;
            try {
                modules = SaveLoad.lireModulesDepuisFichier(fich);
            } catch (Exception ex) {
                showError("Erreur lecture fichier : " + ex.getMessage());
                return;
            }
            if (modules.isEmpty()) { showError("Aucun module trouvé dans le fichier."); return; }

            Semestre sem = currSection.semesters.computeIfAbsent(numSem, Semestre::new);
            for (Module m : modules) sem.ajouterNomModules(m.getNom());

            int total = 0;
            for (Groupe g : currSection.groupes.values()) {
                for (Etudiant et : g.etudiants) {
                    for (Module m : modules) et.ajouterModule(m);
                    total++;
                }
            }

            StringBuilder log = new StringBuilder();
            log.append("✅ Import terminé — Semestre ").append(numSem).append("\n");
            log.append("   Modules importés : ").append(modules.size()).append("\n");
            log.append("   Étudiants mis à jour : ").append(total).append("\n");
            log.append("   Groupes concernés : ").append(currSection.groupes.size()).append("\n\n");
            log.append("Modules :\n");
            for (Module m : modules) log.append("   • ").append(m.getNom())
                    .append(" (").append(m.getCredit()).append(" cr)\n");
            taLog.setText(log.toString());
            setStatus("Import réussi : " + modules.size() + " modules.");
        });

        root.add(card, "ac");
        return root;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TAB: STATISTICS
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildStatsPanel() {
        JPanel root = makeContentRoot("Statistiques");

        JPanel card = makeCard(-1, -1);
        card.setLayout(new BorderLayout(0, 14));

        JButton btnRefStats = makeButton("Actualiser les Statistiques", C_ACCENT);
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT)); topBar.setOpaque(false);
        topBar.add(btnRefStats);
        card.add(topBar, BorderLayout.NORTH);

        // Summary counters row
        JPanel counters = new JPanel(new GridLayout(1, 4, 12, 0));
        counters.setOpaque(false);
        JLabel[] counterCards = new JLabel[4];
        String[] counterTitles = {"Groupes","Étudiants","Semestres","Modules moy./étud."};
        Color[]  counterColors = {C_ACCENT, C_ACCENT2, C_WARN, C_DANGER};
        for (int i = 0; i < 4; i++) {
            JPanel cc = new JPanel(new BorderLayout());
            cc.setBackground(counterColors[i]);
            cc.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
            cc.setBorder(new LineBorder(counterColors[i].darker(), 1, true));
            JLabel val = new JLabel("—");
            val.setFont(new Font("Segoe UI", Font.BOLD, 28));
            val.setForeground(Color.WHITE);
            JLabel lbl = new JLabel(counterTitles[i]);
            lbl.setFont(FONT_SMALL);
            lbl.setForeground(new Color(255, 255, 255, 200));
            cc.setBackground(counterColors[i]);
            cc.add(val, BorderLayout.CENTER);
            cc.add(lbl, BorderLayout.SOUTH);
            counterCards[i] = val;
            counters.add(cc);
        }
        card.add(counters, BorderLayout.CENTER);

        // Detail table
        String[] statCols = {"Groupe", "Étudiants", "Capacité libre", "Moyenne du groupe", "Min", "Max"};
        DefaultTableModel statModel = new DefaultTableModel(statCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable statTable = new JTable(statModel);
        styleTable(statTable);
        card.add(new JScrollPane(statTable), BorderLayout.SOUTH);

        btnRefStats.addActionListener(e -> {
            if (currSection == null) { showError("Aucune section active."); return; }
            // Counters
            int nbGroupes = currSection.groupes.size();
            int nbEtuds = currSection.groupes.values().stream()
                    .mapToInt(g -> g.etudiants.size()).sum();
            int nbSems = currSection.semesters.size();
            double avgMods = nbEtuds == 0 ? 0 :
                    currSection.groupes.values().stream()
                            .flatMap(g -> g.etudiants.stream())
                            .mapToInt(et -> et.modules.size()).average().orElse(0);
            counterCards[0].setText(String.valueOf(nbGroupes));
            counterCards[1].setText(String.valueOf(nbEtuds));
            counterCards[2].setText(String.valueOf(nbSems));
            counterCards[3].setText(String.format("%.1f", avgMods));

            statModel.setRowCount(0);
            for (Groupe g : currSection.groupes.values()) {
                double moy = g.calculerMoyenneGeneraldeGroupe();
                double min = g.etudiants.stream()
                        .mapToDouble(Etudiant::calculerMoyenneGenerale).min().orElse(0);
                double max = g.etudiants.stream()
                        .mapToDouble(Etudiant::calculerMoyenneGenerale).max().orElse(0);
                statModel.addRow(new Object[]{
                        g.nomGroupe, g.etudiants.size(), 30 - g.etudiants.size(),
                        String.format("%.2f", moy),
                        String.format("%.2f", min),
                        String.format("%.2f", max)
                });
            }
        });

        root.add(card, "stats");
        return root;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TAB: SAVE / LOAD
    // ══════════════════════════════════════════════════════════════════════
    private JPanel buildSaveLoadPanel() {
        JPanel root = makeContentRoot("Sauvegarde & Chargement");

        // ── Save
        JPanel saveCard = makeCard(520, -1);
        saveCard.setLayout(new BoxLayout(saveCard, BoxLayout.Y_AXIS));
        sectionTitle(saveCard, "💾  Sauvegarder la Section");
        JLabel saveLbl = new JLabel("La section active sera sauvegardée dans un fichier .dat");
        saveLbl.setFont(FONT_BODY); saveLbl.setForeground(C_MUTED);
        saveLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveCard.add(saveLbl); saveCard.add(Box.createVerticalStrut(14));
        JButton btnSave = makeButton("💾  Sauvegarder", C_ACCENT);
        btnSave.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveCard.add(btnSave);

        btnSave.addActionListener(e -> {
            if (currSection == null) { showError("Aucune section active."); return; }
            try {
                SaveLoad.saveToFile(currSection);
                setStatus("Section sauvegardée dans " + currSection.nomSection + ".dat");
                showSuccess("Sauvegarde réussie : " + currSection.nomSection + ".dat");
            } catch (IOException ex) {
                showError("Erreur lors de la sauvegarde : " + ex.getMessage());
            }
        });

        // ── Load
        JPanel loadCard = makeCard(520, -1);
        loadCard.setLayout(new BoxLayout(loadCard, BoxLayout.Y_AXIS));
        sectionTitle(loadCard, "📂  Charger une Section");
        loadCard.add(makeFieldLabel("Nom de la section *"));
        JTextField tfLoadNom = makeTextField("Nom de la section");
        tfLoadNom.setAlignmentX(Component.LEFT_ALIGNMENT);
        loadCard.add(tfLoadNom); loadCard.add(Box.createVerticalStrut(14));
        JButton btnLoad = makeButton("📂  Charger", C_ACCENT2);
        btnLoad.setAlignmentX(Component.LEFT_ALIGNMENT);
        loadCard.add(btnLoad);

        btnLoad.addActionListener(e -> {
            String nom = tfLoadNom.getText().trim();
            if (nom.isEmpty()) { showError("Entrez le nom de la section."); return; }
            try {
                currSection = SaveLoad.loadFromFile(nom);
                lblSectionName.setText("  📋  " + currSection.nomSection + " — Année " + currSection.niveau);
                refreshStudentTable();
                refreshGroupTable();
                tfLoadNom.setText("");
                setStatus("Section chargée : " + currSection.nomSection);
                showSuccess("Section chargée avec succès !");
            } catch (Exception ex) {
                showError("Erreur lors du chargement : " + ex.getMessage());
            }
        });

        root.add(saveCard, "sv");
        root.add(loadCard, "lv");
        return root;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  HELPER BUILDERS
    // ══════════════════════════════════════════════════════════════════════

    /** Creates a scrollable content root with a page title and vertical scroll */
    private JPanel makeContentRoot(String title) {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(C_BG);
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lbl.setForeground(C_TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(lbl);
        root.add(Box.createVerticalStrut(18));

        // Wrap in a scroll pane later — but for now return a panel
        // (wrapping is done by each caller adding to a JScrollPane if needed)
        return root;
    }

    private JPanel makeCard(int prefW, int prefH) {
        JPanel card = new JPanel();
        card.setBackground(C_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(C_BORDER, 1, true),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (prefW > 0) card.setMaximumSize(new Dimension(prefW, prefH > 0 ? prefH : Integer.MAX_VALUE));
        return card;
    }

    private JPanel makeFormRow() {
        JPanel r = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        r.setOpaque(false);
        r.setAlignmentX(Component.LEFT_ALIGNMENT);
        return r;
    }

    private JPanel labeledField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(C_MUTED);
        if (field instanceof JTextField) ((JTextField) field).setPreferredSize(new Dimension(160, 32));
        if (field instanceof JSpinner)   field.setPreferredSize(new Dimension(100, 32));
        if (field instanceof JComboBox)  field.setPreferredSize(new Dimension(120, 32));
        p.add(lbl,   BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JLabel makeFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SECTION);
        l.setForeground(C_TEXT_DARK);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField makeTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(C_BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        // Placeholder
        tf.setForeground(C_MUTED);
        tf.setText(placeholder);
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) { tf.setText(""); tf.setForeground(C_TEXT_DARK); }
            }
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) { tf.setText(placeholder); tf.setForeground(C_MUTED); }
            }
        });
        return tf;
    }

    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_SECTION);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    private void sectionTitle(JPanel p, String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SECTION);
        l.setForeground(C_TEXT_DARK);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(12));
    }

    private void sectionTitleBorder(JPanel p, String text, Object constraint) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SECTION);
        l.setForeground(C_TEXT_DARK);
        p.add(l, constraint);
    }

    private void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(30);
        table.setGridColor(C_BORDER);
        table.setShowVerticalLines(false);
        table.setBackground(C_CARD);
        table.setForeground(C_TEXT_DARK);
        table.setSelectionBackground(new Color(0xD6E8FF));
        table.setSelectionForeground(C_TEXT_DARK);
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(0xEBF0FB));
        header.setForeground(C_TEXT_DARK);
        header.setBorder(new MatteBorder(0, 0, 2, 0, C_BORDER));
    }

    private void clearFields(JTextField... fields) {
        for (JTextField f : fields) f.setText("");
    }

    private void setStatus(String msg) {
        statusLabel.setText("  " + msg);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ENTRY POINT
    // ══════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(View::new);
    }
}