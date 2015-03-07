package core;

import java.io.Serializable;

public final class Pair<A, B> implements Serializable {
    private static final long serialVersionUID = 1L;
    public final A _first;
    public final B _second;

    public Pair(A first, B second) {
        _first = first;
        _second = second;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final Pair<A, B> other = (Pair<A, B>) obj;
        if (_first != other._first && (_first == null || !_first.equals(other._first))) {
            return false;
        }

        if (_second != other._second && (_second == null || !_second.equals(other._second))) {
            return false;
        }

        return true;
    }

}
