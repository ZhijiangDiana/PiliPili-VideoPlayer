package utils;

import androidx.annotation.NonNull;
import org.jetbrains.annotations.NotNull;

public class Pair<K, V>{
    public K first;
    public V second;
    public Pair(K first, V second){
        this.first = first;
        this.second = second;
    }

    public Pair(){}

    @NonNull
    @NotNull
    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
