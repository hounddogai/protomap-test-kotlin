package com.acme.transport.customs.broker.rollup

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker rollups by code. */
interface BrokerRollupStore {
    fun find(code: String): BrokerRollup?
    fun save(item: BrokerRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerRollupStore(
    var items: MutableMap<String, BrokerRollup>,
    var order: MutableList<String>,
) : BrokerRollupStore {
    override fun find(code: String): BrokerRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerRollup) {
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

    override fun list(): MutableList<BrokerRollup> {
        val out = mutableListOf<BrokerRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerRollupStore(): BrokerRollupStore {
    return MemoryBrokerRollupStore(mutableMapOf<String, BrokerRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerRollupStore(
    var inner: BrokerRollupStore,
    var cache: TtlCache<BrokerRollup>,
    var hits: Int,
    var misses: Int,
) : BrokerRollupStore {
    override fun find(code: String): BrokerRollup? {
        val hit = (cache.get(code) as BrokerRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerRollup> = inner.list()
}

fun newCachedBrokerRollupStore(inner: BrokerRollupStore, ttl: Int, clock: Clock): BrokerRollupStore {
    return CachedBrokerRollupStore(
        inner = inner,
        cache = newTtlCache<BrokerRollup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker rollups. Empty fields match all. */
data class BrokerRollupQuery(
    var unit: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerRollup): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerRollup>): MutableList<BrokerRollup> {
        val matched = filter(items) { candidate: BrokerRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerRollups(store: BrokerRollupStore, query: BrokerRollupQuery): MutableList<BrokerRollup> {
    return query.apply(store.list())
}
