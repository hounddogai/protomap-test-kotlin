package com.acme.transport.customs.broker.request

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy broker baselines by code. */
interface LegacyBrokerBaselineStore {
    fun find(code: String): LegacyBrokerBaseline?
    fun save(item: LegacyBrokerBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyBrokerBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyBrokerBaselineStore(
    var items: MutableMap<String, LegacyBrokerBaseline>,
    var order: MutableList<String>,
) : LegacyBrokerBaselineStore {
    override fun find(code: String): LegacyBrokerBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyBrokerBaseline) {
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

    override fun list(): MutableList<LegacyBrokerBaseline> {
        val out = mutableListOf<LegacyBrokerBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyBrokerBaselineStore(): LegacyBrokerBaselineStore {
    return MemoryLegacyBrokerBaselineStore(
        mutableMapOf<String, LegacyBrokerBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyBrokerBaselineStore(
    var inner: LegacyBrokerBaselineStore,
    var cache: TtlCache<LegacyBrokerBaseline>,
    var hits: Int,
    var misses: Int,
) : LegacyBrokerBaselineStore {
    override fun find(code: String): LegacyBrokerBaseline? {
        val hit = (cache.get(code) as LegacyBrokerBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyBrokerBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyBrokerBaseline> = inner.list()
}

fun newCachedLegacyBrokerBaselineStore(inner: LegacyBrokerBaselineStore, ttl: Int, clock: Clock): LegacyBrokerBaselineStore {
    return CachedLegacyBrokerBaselineStore(
        inner = inner,
        cache = newTtlCache<LegacyBrokerBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy broker baselines. Empty fields match all. */
data class LegacyBrokerBaselineQuery(
    var region: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyBrokerBaseline): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyBrokerBaseline>): MutableList<LegacyBrokerBaseline> {
        val matched = filter(items) { candidate: LegacyBrokerBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyBrokerBaselines(store: LegacyBrokerBaselineStore, query: LegacyBrokerBaselineQuery): MutableList<LegacyBrokerBaseline> {
    return query.apply(store.list())
}
