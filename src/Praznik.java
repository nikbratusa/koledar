public class Praznik {
    int dan;
    int mesec;
    int leto;
    boolean sePonavlja;

    public Praznik(int dan, int mesec, int leto, boolean sePonavlja) {
        this.dan = dan;
        this.mesec = mesec;
        this.leto = leto;
        this.sePonavlja = sePonavlja;
    }

    public int getDan() {
        return dan;
    }

    public void setDan(int dan) {
        this.dan = dan;
    }

    public int getLeto() {
        return leto;
    }

    public void setLeto(int leto) {
        this.leto = leto;
    }

    public int getMesec() {
        return mesec;
    }

    public void setMesec(int mesec) {
        this.mesec = mesec;
    }

    public boolean isSePonavlja() {
        return sePonavlja;
    }

    public void setSePonavlja(boolean sePonavlja) {
        this.sePonavlja = sePonavlja;
    }
}
