package pt.isec.pa.deepsea.model.data;

import java.io.Serializable;

public class AreaExploracao implements Serializable {
    private SeaTrench fosso;
    private SeaFloor fundo;

    public AreaExploracao() {
        this.fosso = new SeaTrench();
        this.fundo = new SeaFloor();
    }

    public SeaTrench getFosso() { return fosso; }
    public SeaFloor getFundo() { return fundo; }
}
