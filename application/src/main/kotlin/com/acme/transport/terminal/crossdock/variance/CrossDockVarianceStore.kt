package com.acme.transport.terminal.crossdock.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock variances by code. */
interface CrossDockVarianceStore {
    fun find(code: String): CrossDockVariance?
    fun save(item: CrossDockVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockVarianceStore(
    var items: MutableMap<String, CrossDockVariance>,
    var order: MutableList<String>,
) : CrossDockVarianceStore {
    override fun find(code: String): CrossDockVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockVariance) {
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

    override fun list(): MutableList<CrossDockVariance> {
        val out = mutableListOf<CrossDockVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockVarianceStore(): CrossDockVarianceStore {
    return MemoryCrossDockVarianceStore(mutableMapOf<String, CrossDockVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockVarianceStore(
    var inner: CrossDockVarianceStore,
    var cache: TtlCache<CrossDockVariance>,
    var hits: Int,
    var misses: Int,
) : CrossDockVarianceStore {
    override fun find(code: String): CrossDockVariance? {
        val hit = (cache.get(code) as CrossDockVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockVariance> = inner.list()
}

fun newCachedCrossDockVarianceStore(inner: CrossDockVarianceStore, ttl: Int, clock: Clock): CrossDockVarianceStore {
    return CachedCrossDockVarianceStore(
        inner = inner,
        cache = newTtlCache<CrossDockVariance>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock variances. Empty fields match all. */
data class CrossDockVarianceQuery(
    var name: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockVariance): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockVariance>): MutableList<CrossDockVariance> {
        val matched = filter(items) { candidate: CrossDockVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockVariances(store: CrossDockVarianceStore, query: CrossDockVarianceQuery): MutableList<CrossDockVariance> {
    return query.apply(store.list())
}
