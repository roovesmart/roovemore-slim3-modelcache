package com.appspot.roovemore.slim3.modelcache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.appengine.api.datastore.Key;

public class KeyIterable implements Iterable<Key>{

    protected List<Key> list = new ArrayList<Key>();

    public KeyIterable() {
    }
    public KeyIterable(List<Key> list) {
        this.list.addAll(list);

    }
    public void add(Key key){
        list.add(key);
    }

    public void addAll(List<Key> list){
        this.list.addAll(list);
    }

    public Iterator<Key> iterator() {
        return list.iterator();
    }
}
