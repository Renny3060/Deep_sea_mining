package pt.isec.pa.deepsea.model.data;

public class Constants {

    public static final boolean MODO_DEFESA = true;

    // --- DIMENSÕES DAS GRELHAS ---
    public static final int SURFACE_L = 15; // Linhas da superfície
    public static final int SURFACE_C = 30; // Colunas da superfície

    public static final int FOSSO_P = 20;   // Profundidade do fosso
    public static final int FOSSO_L = 10;   // Largura do fosso

    public static final int FUNDO_DIM = 10; // Fundo do mar é 10x10

    // ---NAVIO E DRONES ---
    public static final double SHIP_FUEL_MAX = 1000.0;
    public static final int DRONES_INICIAIS = 3;
    public static final double DRONE_FUEL_MAX = 100.0;
    public static final double DRONE_HULL_MAX = 100.0;
    public static final double FUEL_INICIAL_DRONE = 100.0;

    // ---RECURSOS E REGRAS ---
    public static final int NUM_ARTEFACTOS = 5;
    public static final int ID_OBSTACULO_START = 1001; // IDs únicos começam aqui

    // --- MINI-JOGO ---
    public static final int PUZZLE_DIM       = 4;
    public static final int MAX_PUZZLE_MOVES = 100; // novo — máximo de movimentos no puzzle

    // --- CONSUMOS DE COMBUSTÍVEL ---
    // (novas — usadas por DeepSeaMining e estados)
    public static final double FUEL_SHIP_MOVE       = 5.0;  // por movimento do navio
    public static final double FUEL_DRONE_MOVE      = 1.0;  // por passo do drone (fosso/fundo)
    public static final double FUEL_REPAIR_COST     = 20.0; // custo de reparação do casco
    public static final double FUEL_MINERAL_COLLECT = 1.0;  // custo extra de recolher minério

    // --- DANOS ---
    // (novas — usadas por DeepSeaMining)
    public static final double DANO_ANIMAL  = 10.0; // dano de animal marinho no fosso
    public static final double DANO_MONSTRO = 5.0;  // dano de monstro no fundo do mar

    // --- UPGRADES DE OFICINA ---
    // (novas — usadas por Drone)
    public static final double UPGRADE_TANK_AMOUNT  = 20.0; // aumento da capacidade do tanque
    public static final double UPGRADE_ARMOR_AMOUNT = 10.0; // aumento da blindagem máxima

    // --- PROBABILIDADES ---
    public static final double PROB_MINERAL = 0.15;
    public static final double PROB_ARTIFACT = 0.02;
    public static final double PROB_MONSTER = 0.05;
    public static final double PROB_ANIMAL = 0.08;
    public static final double PROB_CORRENTE = 0.07;

    // Nome do ficheiro de registo histórico (Log)
    public static final String LOG_FILE = "deepsea_history.txt";

    // Nome do ficheiro predefinido para gravação binária do jogo.
    public static final String SAVE_FILE = "deepsea_save.dat";
}