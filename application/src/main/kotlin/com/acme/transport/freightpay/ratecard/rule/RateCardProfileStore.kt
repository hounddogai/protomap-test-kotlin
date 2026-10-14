package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card profiles by code. */
interface RateCardProfileStore {
    fun find(code: String): RateCardProfile?
    fun save(item: RateCardProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardProfileStore(
    var items: MutableMap<String, RateCardProfile>,
    var order: MutableList<String>,
) : RateCardProfileStore {
    override fun find(code: String): RateCardProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardProfile) {
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

    override fun list(): MutableList<RateCardProfile> {
        val out = mutableListOf<RateCardProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardProfileStore(): RateCardProfileStore {
    return MemoryRateCardProfileStore(mutableMapOf<String, RateCardProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardProfileStore(
    var inner: RateCardProfileStore,
    var cache: TtlCache<RateCardProfile>,
    var hits: Int,
    var misses: Int,
) : RateCardProfileStore {
    override fun find(code: String): RateCardProfile? {
        val hit = (cache.get(code) as RateCardProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardProfile> = inner.list()
}

fun newCachedRateCardProfileStore(inner: RateCardProfileStore, ttl: Int, clock: Clock): RateCardProfileStore {
    return CachedRateCardProfileStore(
        inner = inner,
        cache = newTtlCache<RateCardProfile>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card profiles. Empty fields match all. */
data class RateCardProfileQuery(
    var title: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardProfile): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardProfile>): MutableList<RateCardProfile> {
        val matched = filter(items) { candidate: RateCardProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardProfiles(store: RateCardProfileStore, query: RateCardProfileQuery): MutableList<RateCardProfile> {
    return query.apply(store.list())
}
