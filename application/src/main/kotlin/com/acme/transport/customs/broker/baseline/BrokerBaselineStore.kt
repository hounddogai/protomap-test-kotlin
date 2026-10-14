package com.acme.transport.customs.broker.baseline

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker baselines by code. */
interface BrokerBaselineStore {
    fun find(code: String): BrokerBaseline?
    fun save(item: BrokerBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerBaselineStore(
    var items: MutableMap<String, BrokerBaseline>,
    var order: MutableList<String>,
) : BrokerBaselineStore {
    override fun find(code: String): BrokerBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerBaseline) {
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

    override fun list(): MutableList<BrokerBaseline> {
        val out = mutableListOf<BrokerBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerBaselineStore(): BrokerBaselineStore {
    return MemoryBrokerBaselineStore(mutableMapOf<String, BrokerBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerBaselineStore(
    var inner: BrokerBaselineStore,
    var cache: TtlCache<BrokerBaseline>,
    var hits: Int,
    var misses: Int,
) : BrokerBaselineStore {
    override fun find(code: String): BrokerBaseline? {
        val hit = (cache.get(code) as BrokerBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerBaseline> = inner.list()
}

fun newCachedBrokerBaselineStore(inner: BrokerBaselineStore, ttl: Int, clock: Clock): BrokerBaselineStore {
    return CachedBrokerBaselineStore(
        inner = inner,
        cache = newTtlCache<BrokerBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker baselines. Empty fields match all. */
data class BrokerBaselineQuery(
    var country: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerBaseline): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerBaseline>): MutableList<BrokerBaseline> {
        val matched = filter(items) { candidate: BrokerBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerBaselines(store: BrokerBaselineStore, query: BrokerBaselineQuery): MutableList<BrokerBaseline> {
    return query.apply(store.list())
}
