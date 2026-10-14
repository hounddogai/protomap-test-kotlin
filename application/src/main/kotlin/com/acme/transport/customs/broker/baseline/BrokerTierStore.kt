package com.acme.transport.customs.broker.baseline

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker tiers by code. */
interface BrokerTierStore {
    fun find(code: String): BrokerTier?
    fun save(item: BrokerTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerTierStore(
    var items: MutableMap<String, BrokerTier>,
    var order: MutableList<String>,
) : BrokerTierStore {
    override fun find(code: String): BrokerTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerTier) {
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

    override fun list(): MutableList<BrokerTier> {
        val out = mutableListOf<BrokerTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerTierStore(): BrokerTierStore {
    return MemoryBrokerTierStore(mutableMapOf<String, BrokerTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerTierStore(
    var inner: BrokerTierStore,
    var cache: TtlCache<BrokerTier>,
    var hits: Int,
    var misses: Int,
) : BrokerTierStore {
    override fun find(code: String): BrokerTier? {
        val hit = (cache.get(code) as BrokerTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerTier> = inner.list()
}

fun newCachedBrokerTierStore(inner: BrokerTierStore, ttl: Int, clock: Clock): BrokerTierStore {
    return CachedBrokerTierStore(
        inner = inner,
        cache = newTtlCache<BrokerTier>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker tiers. Empty fields match all. */
data class BrokerTierQuery(
    var name: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerTier): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<BrokerTier>): MutableList<BrokerTier> {
        val matched = filter(items) { candidate: BrokerTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerTiers(store: BrokerTierStore, query: BrokerTierQuery): MutableList<BrokerTier> {
    return query.apply(store.list())
}
