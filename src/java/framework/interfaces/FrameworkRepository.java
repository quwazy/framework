package framework.interfaces;

import java.util.List;

public interface FrameworkRepository<T> {
    /// GET ONE
    T get(int id);
    /// GET ALL
    List<T> getAll();
    /// ADD ONE
    void add(T object);
    /// UPDATE ONE
    void update(int id, T object);
    /// DELETE ONE
    void delete(int id);
}
