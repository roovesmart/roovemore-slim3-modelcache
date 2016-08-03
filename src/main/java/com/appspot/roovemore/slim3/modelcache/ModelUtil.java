package com.appspot.roovemore.slim3.modelcache;

import org.slim3.datastore.Model;

public class ModelUtil {

	public static <T> boolean isModelClass(Class<T> modelClass) {
		if (modelClass == null)
			return false;
		Model modelAnnotation = modelClass.getAnnotation(Model.class);
		if (modelAnnotation == null)
			return false;
		return true;
	}

	public static <T> void checkModelClass(Class<T> modelClass) {
		boolean flg = isModelClass(modelClass);
		if (!flg) {
			throw new NullPointerException(
					"param modelClass is not ModelClass. param modelClass = "
							+ modelClass);
		}
	}
}
