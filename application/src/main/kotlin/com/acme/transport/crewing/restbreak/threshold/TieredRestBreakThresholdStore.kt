package com.acme.transport.crewing.restbreak.threshold

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered rest break thresholds by code. */
interface TieredRestBreakThresholdStore {
    fun find(code: String): TieredRestBreakThreshold?
    fun save(item: TieredRestBreakThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredRestBreakThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredRestBreakThresholdStore(
    var items: MutableMap<String, TieredRestBreakThreshold>,
    var order: MutableList<String>,
) : TieredRestBreakThresholdStore {
    override fun find(code: String): TieredRestBreakThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredRestBreakThreshold) {
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

    override fun list(): MutableList<TieredRestBreakThreshold> {
        val out = mutableListOf<TieredRestBreakThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredRestBreakThresholdStore(): TieredRestBreakThresholdStore {
    return MemoryTieredRestBreakThresholdStore(
        mutableMapOf<String, TieredRestBreakThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredRestBreakThresholdStore(
    var inner: TieredRestBreakThresholdStore,
    var cache: TtlCache<TieredRestBreakThreshold>,
    var hits: Int,
    var misses: Int,
) : TieredRestBreakThresholdStore {
    override fun find(code: String): TieredRestBreakThreshold? {
        val hit = (cache.get(code) as TieredRestBreakThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredRestBreakThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredRestBreakThreshold> = inner.list()
}

fun newCachedTieredRestBreakThresholdStore(inner: TieredRestBreakThresholdStore, ttl: Int, clock: Clock): TieredRestBreakThresholdStore {
    return CachedTieredRestBreakThresholdStore(
        inner = inner,
        cache = newTtlCache<TieredRestBreakThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered rest break thresholds. Empty fields match all. */
data class TieredRestBreakThresholdQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredRestBreakThreshold): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<TieredRestBreakThreshold>): MutableList<TieredRestBreakThreshold> {
        val matched = filter(items) { candidate: TieredRestBreakThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredRestBreakThresholds(store: TieredRestBreakThresholdStore, query: TieredRestBreakThresholdQuery): MutableList<TieredRestBreakThreshold> {
    return query.apply(store.list())
}
