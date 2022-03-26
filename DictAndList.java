import java.util.ArrayList;
import java.util.Map;

/*
        Clasa care cuprinde datele procesate in MAP
* */
public class DictAndList {
    Map<Integer, Integer> dictionary;
    ArrayList<String> list;

    public DictAndList(Map<Integer, Integer> dictionary, ArrayList<String> list) {
        this.dictionary = dictionary;
        this.list = list;
    }

    @Override
    public String toString() {
        return "DictAndList{" +
                "dictionary=" + dictionary +
                ", list=" + list +
                '}';
    }
}
