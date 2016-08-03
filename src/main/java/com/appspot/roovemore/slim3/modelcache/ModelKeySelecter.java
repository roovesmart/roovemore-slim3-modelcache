package com.appspot.roovemore.slim3.modelcache;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

public class ModelKeySelecter {

	public static <T> List<Key> getKeyList(Class<T> modelClass){

		ModelUtil.checkModelClass(modelClass);

		List<Key> orgKeyList = null;

		DatastoreService dss =
			DatastoreServiceFactory.getDatastoreService();

		// new Queryの引数はKind名
		Query query = new Query(modelClass.getSimpleName());
		query.setKeysOnly();
		PreparedQuery pq = dss.prepare(query);

		orgKeyList = new ArrayList<Key>();
		for (Entity e : pq.asIterable()) {
			// 取り出したエンティティーからキーを取得する
			orgKeyList.add(e.getKey());
		}

		return orgKeyList;
	}


}
