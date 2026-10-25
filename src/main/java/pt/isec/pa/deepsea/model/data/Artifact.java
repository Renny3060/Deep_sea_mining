package pt.isec.pa.deepsea.model.data;

public class Artifact extends FloorCell {
    private String artifactName; // Usamos artifactName para não confundir com o name do super
    private double value;

    // Construtor completo (passa "X" para o super, que é o símbolo do artefacto)
    public Artifact(String artifactName, double value) {
        super("X");
        this.artifactName = artifactName;
        this.value = value;
    }

    // RESOLVE O ERRO DO SEAFLOOR: Construtor padrão
    public Artifact() {
        super("X"); // Passa o símbolo exigido pelo construtor do FloorCell
        this.artifactName = "Artefacto Desconhecido";
        this.value = 50.0;
    }

    public String getArtifactName() { return artifactName; }
    public double getValue() { return value; }

    @Override
    public String toString() {
        return String.format("%s (%.2f$)", artifactName, value);
    }
}