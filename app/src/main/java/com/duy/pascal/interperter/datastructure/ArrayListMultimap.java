/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.pascal.interperter.datastructure;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Duy on 9/2/2017.
 */

public class ArrayListMultimap<K, V> implements Serializable {
    private HashMap<K, ArrayList<V>> map = new HashMap<>();

    private ArrayListMultimap() {

    }

    @NonNull
    public static <K, V> ArrayListMultimap<K, V> create() {
        return new ArrayListMultimap<>();
    }

    public void put(@NonNull K key, V value) {
        ArrayList<V> list = this.map.get(key);
        if (list == null) {
            list = new ArrayList<>();
            map.put(key, list);
        }
        list.add(value);
    }

    @NonNull
    public ArrayList<V> get(@NonNull K key) {
        ArrayList<V> list = map.get(key);
        if (list == null) {
            list = new ArrayList<>();
            map.put(key, list);
        }
        return list;
    }

    public boolean containsKey(K name) {
        return map.containsKey(name);
    }

    public Collection<ArrayList<V>> values() {
        return map.values();
    }
}
