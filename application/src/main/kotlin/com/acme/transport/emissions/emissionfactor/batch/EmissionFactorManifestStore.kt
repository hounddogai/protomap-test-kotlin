package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor manifests by code. */
interface EmissionFactorManifestStore {
    fun find(code: String): EmissionFactorManifest?
    fun save(item: EmissionFactorManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorManifestStore(
    var items: MutableMap<String, EmissionFactorManifest>,
    var order: MutableList<String>,
) : EmissionFactorManifestStore {
    override fun find(code: String): EmissionFactorManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorManifest) {
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

    override fun list(): MutableList<EmissionFactorManifest> {
        val out = mutableListOf<EmissionFactorManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorManifestStore(): EmissionFactorManifestStore {
    return MemoryEmissionFactorManifestStore(
        mutableMapOf<String, EmissionFactorManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorManifestStore(
    var inner: EmissionFactorManifestStore,
    var cache: TtlCache<EmissionFactorManifest>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorManifestStore {
    override fun find(code: String): EmissionFactorManifest? {
        val hit = (cache.get(code) as EmissionFactorManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorManifest> = inner.list()
}

fun newCachedEmissionFactorManifestStore(inner: EmissionFactorManifestStore, ttl: Int, clock: Clock): EmissionFactorManifestStore {
    return CachedEmissionFactorManifestStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor manifests. Empty fields match all. */
data class EmissionFactorManifestQuery(
    var source: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorManifest): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorManifest>): MutableList<EmissionFactorManifest> {
        val matched = filter(items) { candidate: EmissionFactorManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorManifests(store: EmissionFactorManifestStore, query: EmissionFactorManifestQuery): MutableList<EmissionFactorManifest> {
    return query.apply(store.list())
}
