package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card groups by code. */
interface RateCardGroupStore {
    fun find(code: String): RateCardGroup?
    fun save(item: RateCardGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardGroupStore(
    var items: MutableMap<String, RateCardGroup>,
    var order: MutableList<String>,
) : RateCardGroupStore {
    override fun find(code: String): RateCardGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardGroup) {
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

    override fun list(): MutableList<RateCardGroup> {
        val out = mutableListOf<RateCardGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardGroupStore(): RateCardGroupStore {
    return MemoryRateCardGroupStore(mutableMapOf<String, RateCardGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardGroupStore(
    var inner: RateCardGroupStore,
    var cache: TtlCache<RateCardGroup>,
    var hits: Int,
    var misses: Int,
) : RateCardGroupStore {
    override fun find(code: String): RateCardGroup? {
        val hit = (cache.get(code) as RateCardGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardGroup> = inner.list()
}

fun newCachedRateCardGroupStore(inner: RateCardGroupStore, ttl: Int, clock: Clock): RateCardGroupStore {
    return CachedRateCardGroupStore(
        inner = inner,
        cache = newTtlCache<RateCardGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card groups. Empty fields match all. */
data class RateCardGroupQuery(
    var depot: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardGroup): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<RateCardGroup>): MutableList<RateCardGroup> {
        val matched = filter(items) { candidate: RateCardGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardGroups(store: RateCardGroupStore, query: RateCardGroupQuery): MutableList<RateCardGroup> {
    return query.apply(store.list())
}
