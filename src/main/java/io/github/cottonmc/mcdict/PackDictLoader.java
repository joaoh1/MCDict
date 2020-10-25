package io.github.cottonmc.mcdict;

import io.github.cottonmc.mcdict.api.Dict;
import io.github.cottonmc.mcdict.api.DictManager;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class PackDictLoader implements SimpleResourceReloadListener<Map<String, Map<Identifier, List<JsonObject>>>> {
	private static final String DATA_TYPE = "dicts/";
	private static final String EXTENSION = ".json";

	@Override
	public CompletableFuture<Map<String, Map<Identifier, List<JsonObject>>>> load(ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			GsonBuilder builder = new GsonBuilder().setLenient().setPrettyPrinting().serializeNulls();
			for (Function<GsonBuilder, GsonBuilder> factory : DictManager.FACTORIES) {
				factory.apply(builder);
			}
			Gson gson = builder.create();
			Map<String, Map<Identifier, List<JsonObject>>> ret = new HashMap<>();
			for (String key : DictManager.DICT_TYPES.keySet()) {
				Map<Identifier, List<JsonObject>> values = new HashMap<>();
				Collection<Identifier> resources = manager.findResources(DATA_TYPE + key, name -> name.endsWith(EXTENSION));
				for (Identifier id : resources) {
					Identifier newId = new Identifier(id.getNamespace(), id.getPath().substring(DATA_TYPE.length() + key.length() + 1, id.getPath().length() - EXTENSION.length()));
					if (!DictManager.DATA_PACK.dicts.get(key).containsKey(newId)) {
						MCDict.logger.error("[MCDict] Tried to load dict " + newId.toString() + " that wasn't registered");
						continue;
					}
					try {
						Collection<Resource> files = manager.getAllResources(id);
						List<JsonObject> allVals = new ArrayList<>();
						for (Resource file : files) {
							Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
							JsonObject json = JsonHelper.deserialize(gson, reader, JsonObject.class);
							allVals.add(json);
						}
						values.put(newId, allVals);
					} catch (IOException | JsonParseException e) {
						MCDict.logger.error("[MCDict] Failed to load file(s) for dict " + id.toString() + ": " + e.getMessage());
					}
				}
				ret.put(key, values);
			}
			return ret;
		});
	}

	@Override
	public CompletableFuture<Void> apply(Map<String, Map<Identifier, List<JsonObject>>> dicts, ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			for (String type : dicts.keySet()) {
				Map<Identifier, Dict<?, ?>> registered = DictManager.DATA_PACK.dicts.get(type);
				registered.forEach((id, dict) -> dict.clear());
				Map<Identifier, List<JsonObject>> typeDicts = dicts.get(type);
				for (Identifier id : typeDicts.keySet()) {
					Dict<?, ?> dict = registered.get(id);
					List<JsonObject> jsons = typeDicts.get(id);
					for (JsonObject json : jsons) {
						boolean replace = json.get("replace").getAsBoolean();
						boolean override = json.get("override").getAsBoolean();
						JsonObject vals = json.getAsJsonObject("values");
						try {
							dict.fromJson(replace, override, vals);
						} catch (JsonParseException e) {
							MCDict.logger.error("[MCDict] Failed to load {} dict {}: {}", type, id.toString(), e.getMessage());
						}
					}
				}
			}
		});
	}

	@Override
	public Identifier getFabricId() {
		return new Identifier(MCDict.MODID, "dict_loader");
	}
}
