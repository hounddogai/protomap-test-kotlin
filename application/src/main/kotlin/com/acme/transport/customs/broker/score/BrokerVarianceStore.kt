package com.acme.transport.customs.broker.score

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker variances by code. */
interface BrokerVarianceStore {
    fun find(code: String): BrokerVariance?
    fun save(item: BrokerVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerVarianceStore(
    var items: MutableMap<String, BrokerVariance>,
    var order: MutableList<String>,
) : BrokerVarianceStore {
    override fun find(code: String): BrokerVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerVariance) {
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

    override fun list(): MutableList<BrokerVariance> {
        val out = mutableListOf<BrokerVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerVarianceStore(): BrokerVarianceStore {
    return MemoryBrokerVarianceStore(mutableMapOf<String, BrokerVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerVarianceStore(
    var inner: BrokerVarianceStore,
    var cache: TtlCache<BrokerVariance>,
    var hits: Int,
    var misses: Int,
) : BrokerVarianceStore {
    override fun find(code: String): BrokerVariance? {
        val hit = (cache.get(code) as BrokerVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerVariance> = inner.list()
}

fun newCachedBrokerVarianceStore(inner: BrokerVarianceStore, ttl: Int, clock: Clock): BrokerVarianceStore {
    return CachedBrokerVarianceStore(
        inner = inner,
        cache = newTtlCache<BrokerVariance>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker variances. Empty fields match all. */
data class BrokerVarianceQuery(
    var title: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerVariance): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerVariance>): MutableList<BrokerVariance> {
        val matched = filter(items) { candidate: BrokerVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerVariances(store: BrokerVarianceStore, query: BrokerVarianceQuery): MutableList<BrokerVariance> {
    return query.apply(store.list())
}
