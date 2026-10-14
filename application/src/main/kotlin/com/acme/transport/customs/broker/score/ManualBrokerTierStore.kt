package com.acme.transport.customs.broker.score

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual broker tiers by code. */
interface ManualBrokerTierStore {
    fun find(code: String): ManualBrokerTier?
    fun save(item: ManualBrokerTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualBrokerTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualBrokerTierStore(
    var items: MutableMap<String, ManualBrokerTier>,
    var order: MutableList<String>,
) : ManualBrokerTierStore {
    override fun find(code: String): ManualBrokerTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualBrokerTier) {
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

    override fun list(): MutableList<ManualBrokerTier> {
        val out = mutableListOf<ManualBrokerTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualBrokerTierStore(): ManualBrokerTierStore {
    return MemoryManualBrokerTierStore(mutableMapOf<String, ManualBrokerTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualBrokerTierStore(
    var inner: ManualBrokerTierStore,
    var cache: TtlCache<ManualBrokerTier>,
    var hits: Int,
    var misses: Int,
) : ManualBrokerTierStore {
    override fun find(code: String): ManualBrokerTier? {
        val hit = (cache.get(code) as ManualBrokerTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualBrokerTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualBrokerTier> = inner.list()
}

fun newCachedManualBrokerTierStore(inner: ManualBrokerTierStore, ttl: Int, clock: Clock): ManualBrokerTierStore {
    return CachedManualBrokerTierStore(
        inner = inner,
        cache = newTtlCache<ManualBrokerTier>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual broker tiers. Empty fields match all. */
data class ManualBrokerTierQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualBrokerTier): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<ManualBrokerTier>): MutableList<ManualBrokerTier> {
        val matched = filter(items) { candidate: ManualBrokerTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualBrokerTiers(store: ManualBrokerTierStore, query: ManualBrokerTierQuery): MutableList<ManualBrokerTier> {
    return query.apply(store.list())
}
