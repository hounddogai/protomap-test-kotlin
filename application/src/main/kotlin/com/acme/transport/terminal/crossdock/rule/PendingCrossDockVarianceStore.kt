package com.acme.transport.terminal.crossdock.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps pending cross dock variances by code. */
interface PendingCrossDockVarianceStore {
    fun find(code: String): PendingCrossDockVariance?
    fun save(item: PendingCrossDockVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingCrossDockVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingCrossDockVarianceStore(
    var items: MutableMap<String, PendingCrossDockVariance>,
    var order: MutableList<String>,
) : PendingCrossDockVarianceStore {
    override fun find(code: String): PendingCrossDockVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingCrossDockVariance) {
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

    override fun list(): MutableList<PendingCrossDockVariance> {
        val out = mutableListOf<PendingCrossDockVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingCrossDockVarianceStore(): PendingCrossDockVarianceStore {
    return MemoryPendingCrossDockVarianceStore(
        mutableMapOf<String, PendingCrossDockVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingCrossDockVarianceStore(
    var inner: PendingCrossDockVarianceStore,
    var cache: TtlCache<PendingCrossDockVariance>,
    var hits: Int,
    var misses: Int,
) : PendingCrossDockVarianceStore {
    override fun find(code: String): PendingCrossDockVariance? {
        val hit = (cache.get(code) as PendingCrossDockVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingCrossDockVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingCrossDockVariance> = inner.list()
}

fun newCachedPendingCrossDockVarianceStore(inner: PendingCrossDockVarianceStore, ttl: Int, clock: Clock): PendingCrossDockVarianceStore {
    return CachedPendingCrossDockVarianceStore(
        inner = inner,
        cache = newTtlCache<PendingCrossDockVariance>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending cross dock variances. Empty fields match all. */
data class PendingCrossDockVarianceQuery(
    var locale: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingCrossDockVariance): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingCrossDockVariance>): MutableList<PendingCrossDockVariance> {
        val matched = filter(items) { candidate: PendingCrossDockVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingCrossDockVariances(store: PendingCrossDockVarianceStore, query: PendingCrossDockVarianceQuery): MutableList<PendingCrossDockVariance> {
    return query.apply(store.list())
}
