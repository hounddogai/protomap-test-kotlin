package com.acme.transport.fleetops.tire.tireclaim

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback tire rollups by code. */
interface FallbackTireRollupStore {
    fun find(code: String): FallbackTireRollup?
    fun save(item: FallbackTireRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackTireRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackTireRollupStore(
    var items: MutableMap<String, FallbackTireRollup>,
    var order: MutableList<String>,
) : FallbackTireRollupStore {
    override fun find(code: String): FallbackTireRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackTireRollup) {
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

    override fun list(): MutableList<FallbackTireRollup> {
        val out = mutableListOf<FallbackTireRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackTireRollupStore(): FallbackTireRollupStore {
    return MemoryFallbackTireRollupStore(mutableMapOf<String, FallbackTireRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackTireRollupStore(
    var inner: FallbackTireRollupStore,
    var cache: TtlCache<FallbackTireRollup>,
    var hits: Int,
    var misses: Int,
) : FallbackTireRollupStore {
    override fun find(code: String): FallbackTireRollup? {
        val hit = (cache.get(code) as FallbackTireRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackTireRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackTireRollup> = inner.list()
}

fun newCachedFallbackTireRollupStore(inner: FallbackTireRollupStore, ttl: Int, clock: Clock): FallbackTireRollupStore {
    return CachedFallbackTireRollupStore(
        inner = inner,
        cache = newTtlCache<FallbackTireRollup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback tire rollups. Empty fields match all. */
data class FallbackTireRollupQuery(
    var source: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackTireRollup): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackTireRollup>): MutableList<FallbackTireRollup> {
        val matched = filter(items) { candidate: FallbackTireRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackTireRollups(store: FallbackTireRollupStore, query: FallbackTireRollupQuery): MutableList<FallbackTireRollup> {
    return query.apply(store.list())
}
