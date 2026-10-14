package com.acme.transport.emissions.emissionfactor.manifest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy emission factor manifests by code. */
interface LegacyEmissionFactorManifestStore {
    fun find(code: String): LegacyEmissionFactorManifest?
    fun save(item: LegacyEmissionFactorManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyEmissionFactorManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyEmissionFactorManifestStore(
    var items: MutableMap<String, LegacyEmissionFactorManifest>,
    var order: MutableList<String>,
) : LegacyEmissionFactorManifestStore {
    override fun find(code: String): LegacyEmissionFactorManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyEmissionFactorManifest) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<LegacyEmissionFactorManifest> {
        val out = mutableListOf<LegacyEmissionFactorManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyEmissionFactorManifestStore(): LegacyEmissionFactorManifestStore {
    return MemoryLegacyEmissionFactorManifestStore(
        mutableMapOf<String, LegacyEmissionFactorManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyEmissionFactorManifestStore(
    var inner: LegacyEmissionFactorManifestStore,
    var cache: TtlCache<LegacyEmissionFactorManifest>,
    var hits: Int,
    var misses: Int,
) : LegacyEmissionFactorManifestStore {
    override fun find(code: String): LegacyEmissionFactorManifest? {
        val hit = (cache.get(code) as LegacyEmissionFactorManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyEmissionFactorManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyEmissionFactorManifest> = inner.list()
}

fun newCachedLegacyEmissionFactorManifestStore(inner: LegacyEmissionFactorManifestStore, ttl: Int, clock: Clock): LegacyEmissionFactorManifestStore {
    return CachedLegacyEmissionFactorManifestStore(
        inner = inner,
        cache = newTtlCache<LegacyEmissionFactorManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy emission factor manifests. Empty fields match all. */
data class LegacyEmissionFactorManifestQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyEmissionFactorManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyEmissionFactorManifest>): MutableList<LegacyEmissionFactorManifest> {
        val matched = filter(items) { candidate: LegacyEmissionFactorManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyEmissionFactorManifests(store: LegacyEmissionFactorManifestStore, query: LegacyEmissionFactorManifestQuery): MutableList<LegacyEmissionFactorManifest> {
    return query.apply(store.list())
}
