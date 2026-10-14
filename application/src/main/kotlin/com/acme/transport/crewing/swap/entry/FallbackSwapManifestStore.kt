package com.acme.transport.crewing.swap.entry

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback swap manifests by code. */
interface FallbackSwapManifestStore {
    fun find(code: String): FallbackSwapManifest?
    fun save(item: FallbackSwapManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackSwapManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackSwapManifestStore(
    var items: MutableMap<String, FallbackSwapManifest>,
    var order: MutableList<String>,
) : FallbackSwapManifestStore {
    override fun find(code: String): FallbackSwapManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackSwapManifest) {
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

    override fun list(): MutableList<FallbackSwapManifest> {
        val out = mutableListOf<FallbackSwapManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackSwapManifestStore(): FallbackSwapManifestStore {
    return MemoryFallbackSwapManifestStore(
        mutableMapOf<String, FallbackSwapManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackSwapManifestStore(
    var inner: FallbackSwapManifestStore,
    var cache: TtlCache<FallbackSwapManifest>,
    var hits: Int,
    var misses: Int,
) : FallbackSwapManifestStore {
    override fun find(code: String): FallbackSwapManifest? {
        val hit = (cache.get(code) as FallbackSwapManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackSwapManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackSwapManifest> = inner.list()
}

fun newCachedFallbackSwapManifestStore(inner: FallbackSwapManifestStore, ttl: Int, clock: Clock): FallbackSwapManifestStore {
    return CachedFallbackSwapManifestStore(
        inner = inner,
        cache = newTtlCache<FallbackSwapManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback swap manifests. Empty fields match all. */
data class FallbackSwapManifestQuery(
    var region: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackSwapManifest): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackSwapManifest>): MutableList<FallbackSwapManifest> {
        val matched = filter(items) { candidate: FallbackSwapManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackSwapManifests(store: FallbackSwapManifestStore, query: FallbackSwapManifestQuery): MutableList<FallbackSwapManifest> {
    return query.apply(store.list())
}
