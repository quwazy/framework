package framework.http;

import java.util.HashMap;
import java.util.Set;

/**
 * Class represent one header element.
 */
public class Header {

    protected HashMap<String, String> headers;

    /**
     * Construct header with given parameters.
     */
    public Header() {
        this.headers = new HashMap<>();
    }

    /**
     * Add header pair.
     *
     * @param name: name of the header
     * @param value: value of the header
     */
    public void add(String name, String value) {
        this.headers.put(name, value);
    }

    /**
     * Get header's value
     *
     * @param name: name of the header
     * @return Value of the header
     */
    public String get(String name) {
        return this.headers.get(name);
    }

    /**
     * Get all headers.
     *
     * @return Set of header's keys
     */
    public Set<String> getKeys() {
        return this.headers.keySet();
    }

    @Override
    public String toString() {
        return this.headers.toString();
    }
}