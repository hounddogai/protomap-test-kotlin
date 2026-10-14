package com.acme.transport.freightpay.ratecard.snapshot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card targets by code. */
interface RateCardTargetStore {
    fun find(code: String): RateCardTarget?
    fun save(item: RateCardTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardTargetStore(
    var items: MutableMap<String, RateCardTarget>,
    var order: MutableList<String>,
) : RateCardTargetStore {
    override fun find(code: String): RateCardTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardTarget) {
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

    override fun list(): MutableList<RateCardTarget> {
        val out = mutableListOf<RateCardTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardTargetStore(): RateCardTargetStore {
    return MemoryRateCardTargetStore(mutableMapOf<String, RateCardTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardTargetStore(
    var inner: RateCardTargetStore,
    var cache: TtlCache<RateCardTarget>,
    var hits: Int,
    var misses: Int,
) : RateCardTargetStore {
    override fun find(code: String): RateCardTarget? {
        val hit = (cache.get(code) as RateCardTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardTarget> = inner.list()
}

fun newCachedRateCardTargetStore(inner: RateCardTargetStore, ttl: Int, clock: Clock): RateCardTargetStore {
    return CachedRateCardTargetStore(
        inner = inner,
        cache = newTtlCache<RateCardTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card targets. Empty fields match all. */
data class RateCardTargetQuery(
    var category: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardTarget): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardTarget>): MutableList<RateCardTarget> {
        val matched = filter(items) { candidate: RateCardTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardTargets(store: RateCardTargetStore, query: RateCardTargetQuery): MutableList<RateCardTarget> {
    return query.apply(store.list())
}
