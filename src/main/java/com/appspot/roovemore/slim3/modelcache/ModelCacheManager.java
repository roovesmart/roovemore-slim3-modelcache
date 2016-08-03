package com.appspot.roovemore.slim3.modelcache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slim3.datastore.Datastore;
import org.slim3.memcache.Memcache;

import com.google.appengine.api.datastore.Key;

/**
 * ModelClassをキャッシュするマネージャです。
 *
 * @author toshiba
 *
 */
public class ModelCacheManager {

	// rename 旧メソッド名：getEntityCache
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

			T q2 = null;
			for (Key k : orgKeyList) {

				q2 = (T) cacheMap.get(k);

				if (q2 == null) {

					// もしキャッシュになかったら

					// テーブルより取得し、キャッシュに登録する
					q2 = Datastore.getOrNull(modelClass, k);
					if(q2!=null){
						Memcache.put(k, q2);
						retList.add(q2);
					}

				} else {
					retList.add(q2);
				}
			}

			return retList;
		}
	}

	public static <T> Key putDbCache(Class<T> modelClass, T model) {

		// datastoreに登録する
		Key key = Datastore.put(model);

		// キャッシュに登録する
		Memcache.put(key, model);

		//TODO debug
		Logger.info( "Memcache.get(key) = " +  Memcache.get(key) );

		// キーリストに追加する。
		KeyTableManager.putDb(modelClass, key);

		return key;
	}

	//TODO 汎用キャッシュメソッドは一旦コメントアウト
//	public static <T> void putGeneralCache(String keyName, T o) {
//		Memcache.put(keyName, o);
//	}
//
//	public static <T> void putGeneralCache(Class<?> entity, String keyName, T o) {
//		String key = createGeneralKeyName(entity, keyName);
//		Memcache.put(key, o);
//	}
//
//	public static <T extends Serializable> void putGeneralCache(
//			Class<?> entity, String keyName, List<T> list) throws Exception {
//		String s = S.serializeLIst(list);
//		putGeneralCache(entity, keyName, s);
//	}
//
//	public static <T extends Serializable> void putGeneralCache(String keyName,
//			List<T> list) throws Exception {
//		String s = S.serializeLIst(list);
//		Memcache.put(keyName, s);
//	}
//
//	public static Object getGeneralCache(Class<?> entity, String keyName) {
//		String key = createGeneralKeyName(entity, keyName);
//		return Memcache.get(key);
//	}
//
//	public static Object getGeneralCache(String keyName) {
//		return Memcache.get(keyName);
//	}
//
//	public static <T extends Serializable> List<T> getGeneralCacheList(
//			Class<?> entity, String keyName) throws Exception {
//		String key = createGeneralKeyName(entity, keyName);
//		Object o = Memcache.get(key);
//		return S.deserializeList((String) o);
//	}
//
//	public static <T extends Serializable> List<T> getGeneralCacheList(
//			String keyName) throws Exception {
//		Object o = Memcache.get(keyName);
//		return S.deserializeList((String) o);
//	}
//
//	public static String createGeneralKeyName(Class<?> entity, String keyName) {
//		return entity.getSimpleName() + "_" + keyName;
//	}


}
