package com.acme.transport.customs.broker.review

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker cycles by code. */
interface BrokerCycleStore {
    fun find(code: String): BrokerCycle?
    fun save(item: BrokerCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerCycleStore(
    var items: MutableMap<String, BrokerCycle>,
    var order: MutableList<String>,
) : BrokerCycleStore {
    override fun find(code: String): BrokerCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerCycle) {
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

    override fun list(): MutableList<BrokerCycle> {
        val out = mutableListOf<BrokerCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerCycleStore(): BrokerCycleStore {
    return MemoryBrokerCycleStore(mutableMapOf<String, BrokerCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerCycleStore(
    var inner: BrokerCycleStore,
    var cache: TtlCache<BrokerCycle>,
    var hits: Int,
    var misses: Int,
) : BrokerCycleStore {
    override fun find(code: String): BrokerCycle? {
        val hit = (cache.get(code) as BrokerCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerCycle> = inner.list()
}

fun newCachedBrokerCycleStore(inner: BrokerCycleStore, ttl: Int, clock: Clock): BrokerCycleStore {
    return CachedBrokerCycleStore(
        inner = inner,
        cache = newTtlCache<BrokerCycle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker cycles. Empty fields match all. */
data class BrokerCycleQuery(
    var name: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerCycle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerCycle>): MutableList<BrokerCycle> {
        val matched = filter(items) { candidate: BrokerCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerCycles(store: BrokerCycleStore, query: BrokerCycleQuery): MutableList<BrokerCycle> {
    return query.apply(store.list())
}
