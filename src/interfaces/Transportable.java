package interfaces;

import java.util.List;

public interface Transportable {
    void load(List<Loadable>items);
    void unload();
    void transport(String destination);
}
