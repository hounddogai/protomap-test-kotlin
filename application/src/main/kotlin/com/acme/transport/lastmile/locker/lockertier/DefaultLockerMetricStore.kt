package com.acme.transport.lastmile.locker.lockertier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default locker metrics by code. */
interface DefaultLockerMetricStore {
    fun find(code: String): DefaultLockerMetric?
    fun save(item: DefaultLockerMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultLockerMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultLockerMetricStore(
    var items: MutableMap<String, DefaultLockerMetric>,
    var order: MutableList<String>,
) : DefaultLockerMetricStore {
    override fun find(code: String): DefaultLockerMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultLockerMetric) {
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

    override fun list(): MutableList<DefaultLockerMetric> {
        val out = mutableListOf<DefaultLockerMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultLockerMetricStore(): DefaultLockerMetricStore {
    return MemoryDefaultLockerMetricStore(mutableMapOf<String, DefaultLockerMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultLockerMetricStore(
    var inner: DefaultLockerMetricStore,
    var cache: TtlCache<DefaultLockerMetric>,
    var hits: Int,
    var misses: Int,
) : DefaultLockerMetricStore {
    override fun find(code: String): DefaultLockerMetric? {
        val hit = (cache.get(code) as DefaultLockerMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultLockerMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultLockerMetric> = inner.list()
}

fun newCachedDefaultLockerMetricStore(inner: DefaultLockerMetricStore, ttl: Int, clock: Clock): DefaultLockerMetricStore {
    return CachedDefaultLockerMetricStore(
        inner = inner,
        cache = newTtlCache<DefaultLockerMetric>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default locker metrics. Empty fields match all. */
data class DefaultLockerMetricQuery(
    var locale: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultLockerMetric): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultLockerMetric>): MutableList<DefaultLockerMetric> {
        val matched = filter(items) { candidate: DefaultLockerMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultLockerMetrics(store: DefaultLockerMetricStore, query: DefaultLockerMetricQuery): MutableList<DefaultLockerMetric> {
    return query.apply(store.list())
}
