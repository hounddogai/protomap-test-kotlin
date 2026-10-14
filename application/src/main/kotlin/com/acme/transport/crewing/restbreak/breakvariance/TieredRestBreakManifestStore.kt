package com.acme.transport.crewing.restbreak.breakvariance

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered rest break manifests by code. */
interface TieredRestBreakManifestStore {
    fun find(code: String): TieredRestBreakManifest?
    fun save(item: TieredRestBreakManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredRestBreakManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredRestBreakManifestStore(
    var items: MutableMap<String, TieredRestBreakManifest>,
    var order: MutableList<String>,
) : TieredRestBreakManifestStore {
    override fun find(code: String): TieredRestBreakManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredRestBreakManifest) {
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

    override fun list(): MutableList<TieredRestBreakManifest> {
        val out = mutableListOf<TieredRestBreakManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredRestBreakManifestStore(): TieredRestBreakManifestStore {
    return MemoryTieredRestBreakManifestStore(
        mutableMapOf<String, TieredRestBreakManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredRestBreakManifestStore(
    var inner: TieredRestBreakManifestStore,
    var cache: TtlCache<TieredRestBreakManifest>,
    var hits: Int,
    var misses: Int,
) : TieredRestBreakManifestStore {
    override fun find(code: String): TieredRestBreakManifest? {
        val hit = (cache.get(code) as TieredRestBreakManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredRestBreakManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredRestBreakManifest> = inner.list()
}

fun newCachedTieredRestBreakManifestStore(inner: TieredRestBreakManifestStore, ttl: Int, clock: Clock): TieredRestBreakManifestStore {
    return CachedTieredRestBreakManifestStore(
        inner = inner,
        cache = newTtlCache<TieredRestBreakManifest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered rest break manifests. Empty fields match all. */
data class TieredRestBreakManifestQuery(
    var name: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredRestBreakManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredRestBreakManifest>): MutableList<TieredRestBreakManifest> {
        val matched = filter(items) { candidate: TieredRestBreakManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredRestBreakManifests(store: TieredRestBreakManifestStore, query: TieredRestBreakManifestQuery): MutableList<TieredRestBreakManifest> {
    return query.apply(store.list())
}
