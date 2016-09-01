package com.appspot.roovemore.slim3.modelcache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.slim3.datastore.Datastore;
import org.slim3.memcache.Memcache;

import com.google.appengine.api.datastore.Key;

/**
 * To operate the KeyTable.
 *
 * @see com.appspot.roovemore.slim3.modelcache.KeyTable
 *
 * @author roove
 *
 */
public class KeyTableManager {

	/**
	 * Verify that the KeyTable exist.
	 *
	 * @param keyTable
	 * 			ths keyTable (Model.class)
	 * @return
	 * 			true: exist.
	 * 			false: not exits.
	 */
	public static boolean isExistKeyTable(KeyTable keyTable) {

		if (keyTable == null)
			return false;
		if (keyTable.getEntityKeyList() == null)
			return false;
		if (keyTable.getEntityKeyList().isEmpty())
			return false;

		// TODO deleteした時は、keyTableは更新しているか？
		List<Key> keyList = keyTable.getEntityKeyList();
		for (Key key : keyList) {
			if (key == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Get KeyTable from datastore.
	 *
	 * @param modelClass
	 * 			Acquisition target Model Class.
	 *
	 * @return the KeyTable.
	 */
	public static KeyTable getDb(Class<?> modelClass) {
		ModelUtil.checkModelClass(modelClass);
		return Datastore.getOrNull(KeyTable.class,
				Datastore.createKey(KeyTable.class, modelClass.getSimpleName()
						));
	}

	/**
	 * Get KeyTable from memcache.
	 *
	 * @param modelClass
	 * 			Acquisition target Model Class.
	 *
	 * @return the KeyTable.
	 */
	public static KeyTable getCache(Class<?> modelClass) {
		ModelUtil.checkModelClass(modelClass);

		Key key = Datastore.createKey(KeyTable.class, modelClass.getSimpleName());

		KeyTable keyTable = null;
		keyTable = Memcache.get(key);
		if (keyTable != null) {
			return keyTable;
		}

		// DBよりデータ取得
		keyTable = getDb(modelClass);

		if (isExistKeyTable(keyTable)) {

			// キャッシュに保存する。
			Memcache.put(key, keyTable);

		} else {
			// DBにキー情報が存在しない場合は、オリジナルテーブルから取得しDBに登録する

			// キーリストを取得する
			List<Key> orgKeyList = ModelKeySelecter.getKeyList(modelClass);

			// DBにキーリストを保存
			keyTable = putDbCache(modelClass, orgKeyList);

		}

		return keyTable;
	}

	/**
	 * Get HashSet<Key> from memcache.
	 *
	 * @param modelClass
	 * 			Acquisition target Model Class.
	 *
	 * @return HashSet<Key>.
	 */

	public static HashSet<Key> getCacheModelKeySet(Class<?> modelClass) {

		ModelUtil.checkModelClass(modelClass);

		HashSet<Key> set = new HashSet<Key>();
		KeyTable keyTable = getCache(modelClass);

		if (keyTable == null) {
			return null;
		}
		List<Key> list = keyTable.getEntityKeyList();
		for (Key key : list) {
			set.add(key);
		}
		return set;
	}

	/**
	 * Put datastore and put memcache.
	 *
	 * @param modelClass
	 * 			Memcache target class name.
	 * @param key
	 * 			Memcache target key.
	 * @return
	 * 			memcached KeyTable.
	 */
	public static KeyTable putDbCache(Class<?> modelClass, Key key) {

		ModelUtil.checkModelClass(modelClass);

		List<Key> list = new ArrayList<Key>();
		list.add(key);
		return putDbCache(modelClass, list);
	}

	/**
	 * Put datastore and put memcache.
	 *
	 * @param modelClass
	 * 			Memcache target class name.
	 * @param addKeyList
	 * 			Memcache target keyList.
	 * @return
	 * 			memcached KeyTable.
	 */
	public static KeyTable putDbCache(Class<?> modelClass, List<Key> addKeyList) {

		Logger.info("param modelClass = " + modelClass);
		Logger.info("param paramKeyList = " + addKeyList);

		ModelUtil.checkModelClass(modelClass);

		// キーを生成
		Key keyTableKey = Datastore.createKey(KeyTable.class, modelClass.getSimpleName());

		// キャッシュよりKeyTableを取得
		KeyTable keyTableFromCache = (KeyTable) Memcache.get(keyTableKey);
		Logger.info("keyTable = " + keyTableFromCache);

		if (keyTableFromCache != null) {
			Logger.info("keyTable.getEntityKeyList() = " + keyTableFromCache.getEntityKeyList());

			// KeyTable(キャッシュ)に既に含まれている場合は、更新処理をせずに処理終了
			if (isContainsTargetKeyList(keyTableFromCache.getEntityKeyList(), addKeyList)) {
				return keyTableFromCache;
			}
		}

		// キャッシュに存在しない場合は、DBより取得する。
		KeyTable keyTableFromDb = getDb(modelClass);
		Logger.info("keyTable getDb = " + keyTableFromDb);

		List<Key> keyList = null;

		if (keyTableFromDb == null) {
			// KeyTableに存在しない場合

			keyTableFromDb = new KeyTable();
			keyTableFromDb.setKey(keyTableKey);

			// リストを生成し追加
			keyList = new ArrayList<Key>();
			keyList.addAll(addKeyList);
			keyTableFromDb.setEntityKeyList(keyList);

		} else {
			// すでにエンティティキーが登録されている場合、

			// DBに登録されているキーリストをcontainsチェック用に詰め替える。
			keyList = keyTableFromDb.getEntityKeyList();

			// 今回登録するキーリストがDBに存在しない場合は、登録する。
			for (Key paramKey : addKeyList) {
				if (!keyList.contains(paramKey)) {
					keyList.add(paramKey);
				}
			}
			keyTableFromDb.setEntityKeyList(keyList);
		}

		// DBに保存する。
		Datastore.put(keyTableFromDb);

		// キャッシュにも保存する。
		Memcache.put(keyTableKey, keyTableFromDb);

		Logger.info("keyTableKey = " + keyTableKey);
		Logger.info("keyTable.getEntityKeyList() = " + keyTableFromDb.getEntityKeyList());

		return keyTableFromDb;
	}

	private static boolean isContainsTargetKeyList(List<Key> keyList, List<Key> targetKeyList) {

		// 今回登録するキーリストがキャッシュに存在するかをチェックする
		for (Key targetKey : targetKeyList) {
			if (!keyList.contains(targetKey)) {
				return false;
			}
		}
		return true;
	}


}
