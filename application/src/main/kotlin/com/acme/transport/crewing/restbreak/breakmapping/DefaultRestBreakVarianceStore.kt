package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default rest break variances by code. */
interface DefaultRestBreakVarianceStore {
    fun find(code: String): DefaultRestBreakVariance?
    fun save(item: DefaultRestBreakVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRestBreakVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRestBreakVarianceStore(
    var items: MutableMap<String, DefaultRestBreakVariance>,
    var order: MutableList<String>,
) : DefaultRestBreakVarianceStore {
    override fun find(code: String): DefaultRestBreakVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRestBreakVariance) {
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

    override fun list(): MutableList<DefaultRestBreakVariance> {
        val out = mutableListOf<DefaultRestBreakVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRestBreakVarianceStore(): DefaultRestBreakVarianceStore {
    return MemoryDefaultRestBreakVarianceStore(
        mutableMapOf<String, DefaultRestBreakVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRestBreakVarianceStore(
    var inner: DefaultRestBreakVarianceStore,
    var cache: TtlCache<DefaultRestBreakVariance>,
    var hits: Int,
    var misses: Int,
) : DefaultRestBreakVarianceStore {
    override fun find(code: String): DefaultRestBreakVariance? {
        val hit = (cache.get(code) as DefaultRestBreakVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRestBreakVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRestBreakVariance> = inner.list()
}

fun newCachedDefaultRestBreakVarianceStore(inner: DefaultRestBreakVarianceStore, ttl: Int, clock: Clock): DefaultRestBreakVarianceStore {
    return CachedDefaultRestBreakVarianceStore(
        inner = inner,
        cache = newTtlCache<DefaultRestBreakVariance>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default rest break variances. Empty fields match all. */
data class DefaultRestBreakVarianceQuery(
    var depot: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRestBreakVariance): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultRestBreakVariance>): MutableList<DefaultRestBreakVariance> {
        val matched = filter(items) { candidate: DefaultRestBreakVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRestBreakVariances(store: DefaultRestBreakVarianceStore, query: DefaultRestBreakVarianceQuery): MutableList<DefaultRestBreakVariance> {
    return query.apply(store.list())
}
