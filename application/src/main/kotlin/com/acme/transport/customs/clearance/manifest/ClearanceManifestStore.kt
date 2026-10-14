package com.acme.transport.customs.clearance.manifest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance manifests by code. */
interface ClearanceManifestStore {
    fun find(code: String): ClearanceManifest?
    fun save(item: ClearanceManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceManifestStore(
    var items: MutableMap<String, ClearanceManifest>,
    var order: MutableList<String>,
) : ClearanceManifestStore {
    override fun find(code: String): ClearanceManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceManifest) {
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

    override fun list(): MutableList<ClearanceManifest> {
        val out = mutableListOf<ClearanceManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceManifestStore(): ClearanceManifestStore {
    return MemoryClearanceManifestStore(mutableMapOf<String, ClearanceManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceManifestStore(
    var inner: ClearanceManifestStore,
    var cache: TtlCache<ClearanceManifest>,
    var hits: Int,
    var misses: Int,
) : ClearanceManifestStore {
    override fun find(code: String): ClearanceManifest? {
        val hit = (cache.get(code) as ClearanceManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceManifest> = inner.list()
}

fun newCachedClearanceManifestStore(inner: ClearanceManifestStore, ttl: Int, clock: Clock): ClearanceManifestStore {
    return CachedClearanceManifestStore(
        inner = inner,
        cache = newTtlCache<ClearanceManifest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance manifests. Empty fields match all. */
data class ClearanceManifestQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceManifest>): MutableList<ClearanceManifest> {
        val matched = filter(items) { candidate: ClearanceManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceManifests(store: ClearanceManifestStore, query: ClearanceManifestQuery): MutableList<ClearanceManifest> {
    return query.apply(store.list())
}
