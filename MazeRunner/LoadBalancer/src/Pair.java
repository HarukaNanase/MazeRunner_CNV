public class Pair{
    String key1;
    String key2;
    public Pair(String k1, String k2 ) { this.key1 = k1; this.key2 = k2;}
    public String getKey1(){return this.key1;}
    public String getKey2(){return this.key2;}
    @Override
    public int hashCode(){ return key1.hashCode() ^ key2.hashCode();}
    @Override
    public boolean equals(Object o){
        if(!(o instanceof Pair)) return false;
        Pair pairo = (Pair) o;
        return this.key1.equals(pairo.getKey1()) && this.key2.equals(pairo.getKey2());
    }
}