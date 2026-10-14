package com.acme.transport.customs.broker.audit

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled broker rollups by code. */
interface ScheduledBrokerRollupStore {
    fun find(code: String): ScheduledBrokerRollup?
    fun save(item: ScheduledBrokerRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledBrokerRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledBrokerRollupStore(
    var items: MutableMap<String, ScheduledBrokerRollup>,
    var order: MutableList<String>,
) : ScheduledBrokerRollupStore {
    override fun find(code: String): ScheduledBrokerRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledBrokerRollup) {
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

    override fun list(): MutableList<ScheduledBrokerRollup> {
        val out = mutableListOf<ScheduledBrokerRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledBrokerRollupStore(): ScheduledBrokerRollupStore {
    return MemoryScheduledBrokerRollupStore(
        mutableMapOf<String, ScheduledBrokerRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledBrokerRollupStore(
    var inner: ScheduledBrokerRollupStore,
    var cache: TtlCache<ScheduledBrokerRollup>,
    var hits: Int,
    var misses: Int,
) : ScheduledBrokerRollupStore {
    override fun find(code: String): ScheduledBrokerRollup? {
        val hit = (cache.get(code) as ScheduledBrokerRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledBrokerRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledBrokerRollup> = inner.list()
}

fun newCachedScheduledBrokerRollupStore(inner: ScheduledBrokerRollupStore, ttl: Int, clock: Clock): ScheduledBrokerRollupStore {
    return CachedScheduledBrokerRollupStore(
        inner = inner,
        cache = newTtlCache<ScheduledBrokerRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled broker rollups. Empty fields match all. */
data class ScheduledBrokerRollupQuery(
    var depot: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledBrokerRollup): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledBrokerRollup>): MutableList<ScheduledBrokerRollup> {
        val matched = filter(items) { candidate: ScheduledBrokerRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledBrokerRollups(store: ScheduledBrokerRollupStore, query: ScheduledBrokerRollupQuery): MutableList<ScheduledBrokerRollup> {
    return query.apply(store.list())
}
