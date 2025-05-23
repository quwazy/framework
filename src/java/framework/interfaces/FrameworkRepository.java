package framework.interfaces;

import java.util.List;

public interface FrameworkRepository<T> {
    /// GET ONE
    T get(Long id);
    /// GET ALL
    List<T> getAll();
    /// ADD ONE
    void add(T object);
    /// UPDATE ONE
    void update(Long id, T object);
    /// DELETE ONE
    void delete(Long id);
}
