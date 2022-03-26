/*
        Clasa care cuprinde datele obtinute in REDUCE si necesare formatului de output
* */

public class OutputPackage {
    public float rang;
    public int nrAparitii;
    public int LungimeMaxima;

    public OutputPackage(float rang, int nrAparitii, int lungimeMaxima) {
        this.rang = rang;
        this.nrAparitii = nrAparitii;
        LungimeMaxima = lungimeMaxima;
    }

    @Override
    public String toString() {
        return "OutputPackage{" +
                "rang='" + rang + '\'' +
                ", nrAparitii=" + nrAparitii +
                ", LungimeMaxima=" + LungimeMaxima +
                '}';
    }
}
