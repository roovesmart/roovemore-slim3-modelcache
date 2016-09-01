package com.appspot.roovemore.slim3.modelcache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slim3.datastore.Datastore;
import org.slim3.memcache.Memcache;

import com.google.appengine.api.datastore.Key;

/**
 *
 * This manager class uses slim3's memcache service. and Save the ModelClass to cache.
 *
 * @see org.slim3.datastore.Datastore
 * @see org.slim3.memcache.Memcache
 *
 * @author roove
 *
 */
public class ModelCacheManager {

	/**
	 * Get model class object from memchace.
	 *
	 * @param modelClass
	 *			Returned class name.
	 * @param key
	 * 			memchae key.
	 * @return
	 * 		The model class object acquired from memcache.
	 *
	 */
	public static <T> T getModelFromCache(Class<T> modelClass, Key key) {

		ModelUtil.checkModelClass(modelClass);

		// キャッシュよりデータを取得する
		T model = Memcache.get(key);
		if (model == null) {
			// キャッシュにデータが存在しない場合は、テーブルより取得
			model = Datastore.getOrNull(modelClass, key);
			if (model == null) {
				return null;
			}
			// キャッシュに保存する。
			Memcache.put(key, model);
		}
		return model;
	}

	/**
	 * Get model class objectList from memchace.
	 *
	 * @param modelClass
	 *			Returned class name.
	 * @param orgKeyList
	 * 			memchae keyList.
	 * @return
	 * 		The model class objectList acquired from memcache.
	 */
	public static <T> List<T> getDbList(Class<T> modelClass, List<Key> orgKeyList) {

		ModelUtil.checkModelClass(modelClass);

		if(orgKeyList==null || (orgKeyList.isEmpty())) return null;

		List<T> modelList = new ArrayList<T>();
		for (Key key : orgKeyList) {
			T model = Datastore.getOrNull(modelClass, key);
			if(model!=null){
				modelList.add(model);
			}
		}
		return modelList;
	}
	/**
	 * Get model class objectList from memchace.
	 *
	 * @param modelClass
	 *			Returned class name.
	 * @param orgKeyList
	 * 			memchae keyList.
	 * @return
	 * 		The model class objectList acquired from memcache.
	 */

	/**
	 * Get model class objectList from memchace(from KeyTable).
	 *
	 * @param modelClass
	 *			Returned class name.
	 * @return
	 * 		The model class objectList acquired from memcache.
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> getCacheListFromKeyTable( Class<T> modelClass) {

		ModelUtil.checkModelClass(modelClass);

		KeyTable keyTable = KeyTableManager.getCache(modelClass);

		List<Key> orgKeyList = keyTable.getEntityKeyList();

		// オリジナルデータがレコード0になり、キーリストが生成できない場合はここで終了
		if(orgKeyList.isEmpty()){
			return null;
		}

		KeyIterable orgKeyIt = new KeyIterable(orgKeyList);

		Map<Object, Object> cacheMap = null;

		// キャッシュよりデータを取得する
		cacheMap = Memcache.getAll(orgKeyIt);

		// １件もキャッシュになかったら、DBから取得するが基本的には
		// １件ごとの更新処理でキャッシュクリア、キャッシング処理を実施するため
		// ここでは整合性チェックまでは実施しない。※やろうと思ったがめんどくさかった。
		if (cacheMap.isEmpty()) {

			// キャッシュにデータが存在しない場合は、テーブルより取得
			// 以下のような取得メソッドを使用すると、Keyに該当するレコードがない場合例外をはいてしまう。
			// ※ List<T> retList = Datastore.get(entity, orgKeyIt);

			List<T> retList = new ArrayList<T>();
			for (Key k : orgKeyList) {
				T t = Datastore.getOrNull(modelClass, k);
				if(t!=null){
					retList.add(t);
					cacheMap.put(k, t);
				}
			}
			if (retList.size() == 0) {
				return null;
			}

			// キャッシュに保存する。
			Memcache.putAll(cacheMap);

			return retList;

		} else {

			List<T> retList = new ArrayList<T>();

			T t2 = null;
			for (Key k : orgKeyList) {

				t2 = (T) cacheMap.get(k);

				if (t2 == null) {

					// もしキャッシュになかったら

					// テーブルより取得し、キャッシュに登録する
					t2 = Datastore.getOrNull(modelClass, k);
					if(t2!=null){
						Memcache.put(k, t2);
						retList.add(t2);
					}

				} else {
					retList.add(t2);
				}
			}

			return retList;
		}
	}

	/**
	 * Put datastore and put memcache.
	 *
	 * @param modelClass
	 * 			memcache class name.
	 * @param model
	 * 			put object
	 * @return
	 * 			memcache key.
	 */
	public static <T> Key putDbCache(Class<T> modelClass, T model) {

		// datastoreに登録する
		Key key = Datastore.put(model);

		// キャッシュに登録する
		Memcache.put(key, model);

		//TODO debug
		Logger.info( "Memcache.get(key) = " +  Memcache.get(key) );

		// キーリストに追加する。
		KeyTableManager.putDbCache(modelClass, key);

		return key;
	}


}
