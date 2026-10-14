package com.acme.transport.freightpay.ratecard.policy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card tiers by code. */
interface RateCardTierStore {
    fun find(code: String): RateCardTier?
    fun save(item: RateCardTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardTierStore(
    var items: MutableMap<String, RateCardTier>,
    var order: MutableList<String>,
) : RateCardTierStore {
    override fun find(code: String): RateCardTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardTier) {
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

    override fun list(): MutableList<RateCardTier> {
        val out = mutableListOf<RateCardTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardTierStore(): RateCardTierStore {
    return MemoryRateCardTierStore(mutableMapOf<String, RateCardTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardTierStore(
    var inner: RateCardTierStore,
    var cache: TtlCache<RateCardTier>,
    var hits: Int,
    var misses: Int,
) : RateCardTierStore {
    override fun find(code: String): RateCardTier? {
        val hit = (cache.get(code) as RateCardTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardTier> = inner.list()
}

fun newCachedRateCardTierStore(inner: RateCardTierStore, ttl: Int, clock: Clock): RateCardTierStore {
    return CachedRateCardTierStore(
        inner = inner,
        cache = newTtlCache<RateCardTier>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card tiers. Empty fields match all. */
data class RateCardTierQuery(
    var channel: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardTier): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardTier>): MutableList<RateCardTier> {
        val matched = filter(items) { candidate: RateCardTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardTiers(store: RateCardTierStore, query: RateCardTierQuery): MutableList<RateCardTier> {
    return query.apply(store.list())
}
