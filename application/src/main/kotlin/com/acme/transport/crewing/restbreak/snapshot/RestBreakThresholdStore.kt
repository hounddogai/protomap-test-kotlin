package com.acme.transport.crewing.restbreak.snapshot

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break thresholds by code. */
interface RestBreakThresholdStore {
    fun find(code: String): RestBreakThreshold?
    fun save(item: RestBreakThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakThresholdStore(
    var items: MutableMap<String, RestBreakThreshold>,
    var order: MutableList<String>,
) : RestBreakThresholdStore {
    override fun find(code: String): RestBreakThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakThreshold) {
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

    override fun list(): MutableList<RestBreakThreshold> {
        val out = mutableListOf<RestBreakThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakThresholdStore(): RestBreakThresholdStore {
    return MemoryRestBreakThresholdStore(mutableMapOf<String, RestBreakThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakThresholdStore(
    var inner: RestBreakThresholdStore,
    var cache: TtlCache<RestBreakThreshold>,
    var hits: Int,
    var misses: Int,
) : RestBreakThresholdStore {
    override fun find(code: String): RestBreakThreshold? {
        val hit = (cache.get(code) as RestBreakThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakThreshold> = inner.list()
}

fun newCachedRestBreakThresholdStore(inner: RestBreakThresholdStore, ttl: Int, clock: Clock): RestBreakThresholdStore {
    return CachedRestBreakThresholdStore(
        inner = inner,
        cache = newTtlCache<RestBreakThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break thresholds. Empty fields match all. */
data class RestBreakThresholdQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakThreshold>): MutableList<RestBreakThreshold> {
        val matched = filter(items) { candidate: RestBreakThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakThresholds(store: RestBreakThresholdStore, query: RestBreakThresholdQuery): MutableList<RestBreakThreshold> {
    return query.apply(store.list())
}
