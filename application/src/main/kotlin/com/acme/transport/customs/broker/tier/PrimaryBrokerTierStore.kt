package com.acme.transport.customs.broker.tier

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary broker tiers by code. */
interface PrimaryBrokerTierStore {
    fun find(code: String): PrimaryBrokerTier?
    fun save(item: PrimaryBrokerTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryBrokerTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryBrokerTierStore(
    var items: MutableMap<String, PrimaryBrokerTier>,
    var order: MutableList<String>,
) : PrimaryBrokerTierStore {
    override fun find(code: String): PrimaryBrokerTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryBrokerTier) {
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

    override fun list(): MutableList<PrimaryBrokerTier> {
        val out = mutableListOf<PrimaryBrokerTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryBrokerTierStore(): PrimaryBrokerTierStore {
    return MemoryPrimaryBrokerTierStore(mutableMapOf<String, PrimaryBrokerTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryBrokerTierStore(
    var inner: PrimaryBrokerTierStore,
    var cache: TtlCache<PrimaryBrokerTier>,
    var hits: Int,
    var misses: Int,
) : PrimaryBrokerTierStore {
    override fun find(code: String): PrimaryBrokerTier? {
        val hit = (cache.get(code) as PrimaryBrokerTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryBrokerTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryBrokerTier> = inner.list()
}

fun newCachedPrimaryBrokerTierStore(inner: PrimaryBrokerTierStore, ttl: Int, clock: Clock): PrimaryBrokerTierStore {
    return CachedPrimaryBrokerTierStore(
        inner = inner,
        cache = newTtlCache<PrimaryBrokerTier>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary broker tiers. Empty fields match all. */
data class PrimaryBrokerTierQuery(
    var label: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryBrokerTier): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryBrokerTier>): MutableList<PrimaryBrokerTier> {
        val matched = filter(items) { candidate: PrimaryBrokerTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryBrokerTiers(store: PrimaryBrokerTierStore, query: PrimaryBrokerTierQuery): MutableList<PrimaryBrokerTier> {
    return query.apply(store.list())
}
