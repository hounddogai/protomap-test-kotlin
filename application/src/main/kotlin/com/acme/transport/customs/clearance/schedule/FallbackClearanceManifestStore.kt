package com.acme.transport.customs.clearance.schedule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback clearance manifests by code. */
interface FallbackClearanceManifestStore {
    fun find(code: String): FallbackClearanceManifest?
    fun save(item: FallbackClearanceManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackClearanceManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackClearanceManifestStore(
    var items: MutableMap<String, FallbackClearanceManifest>,
    var order: MutableList<String>,
) : FallbackClearanceManifestStore {
    override fun find(code: String): FallbackClearanceManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackClearanceManifest) {
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

    override fun list(): MutableList<FallbackClearanceManifest> {
        val out = mutableListOf<FallbackClearanceManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackClearanceManifestStore(): FallbackClearanceManifestStore {
    return MemoryFallbackClearanceManifestStore(
        mutableMapOf<String, FallbackClearanceManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackClearanceManifestStore(
    var inner: FallbackClearanceManifestStore,
    var cache: TtlCache<FallbackClearanceManifest>,
    var hits: Int,
    var misses: Int,
) : FallbackClearanceManifestStore {
    override fun find(code: String): FallbackClearanceManifest? {
        val hit = (cache.get(code) as FallbackClearanceManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackClearanceManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackClearanceManifest> = inner.list()
}

fun newCachedFallbackClearanceManifestStore(inner: FallbackClearanceManifestStore, ttl: Int, clock: Clock): FallbackClearanceManifestStore {
    return CachedFallbackClearanceManifestStore(
        inner = inner,
        cache = newTtlCache<FallbackClearanceManifest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback clearance manifests. Empty fields match all. */
data class FallbackClearanceManifestQuery(
    var region: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackClearanceManifest): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackClearanceManifest>): MutableList<FallbackClearanceManifest> {
        val matched = filter(items) { candidate: FallbackClearanceManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackClearanceManifests(store: FallbackClearanceManifestStore, query: FallbackClearanceManifestQuery): MutableList<FallbackClearanceManifest> {
    return query.apply(store.list())
}
