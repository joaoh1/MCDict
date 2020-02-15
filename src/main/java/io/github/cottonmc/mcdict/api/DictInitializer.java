package io.github.cottonmc.mcdict.api;

public interface DictInitializer {
	/**
	 * Called before dicts are registered. If you want to add new serializers/deserializers or registry dict types, you *must* do this now.
	 */
	default void initDictTypes() { }

	/**
	 * Called before static dicts are loaded. You *must* register static dicts now, or you have no guarantee they'll load.
	 */
	void registerDicts();
}
