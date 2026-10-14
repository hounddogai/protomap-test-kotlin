package com.acme.transport.network.cutoff.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff manifests by code. */
interface CutoffManifestStore {
    fun find(code: String): CutoffManifest?
    fun save(item: CutoffManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffManifestStore(
    var items: MutableMap<String, CutoffManifest>,
    var order: MutableList<String>,
) : CutoffManifestStore {
    override fun find(code: String): CutoffManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffManifest) {
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

    override fun list(): MutableList<CutoffManifest> {
        val out = mutableListOf<CutoffManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffManifestStore(): CutoffManifestStore {
    return MemoryCutoffManifestStore(mutableMapOf<String, CutoffManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffManifestStore(
    var inner: CutoffManifestStore,
    var cache: TtlCache<CutoffManifest>,
    var hits: Int,
    var misses: Int,
) : CutoffManifestStore {
    override fun find(code: String): CutoffManifest? {
        val hit = (cache.get(code) as CutoffManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffManifest> = inner.list()
}

fun newCachedCutoffManifestStore(inner: CutoffManifestStore, ttl: Int, clock: Clock): CutoffManifestStore {
    return CachedCutoffManifestStore(
        inner = inner,
        cache = newTtlCache<CutoffManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff manifests. Empty fields match all. */
data class CutoffManifestQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffManifest>): MutableList<CutoffManifest> {
        val matched = filter(items) { candidate: CutoffManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffManifests(store: CutoffManifestStore, query: CutoffManifestQuery): MutableList<CutoffManifest> {
    return query.apply(store.list())
}
