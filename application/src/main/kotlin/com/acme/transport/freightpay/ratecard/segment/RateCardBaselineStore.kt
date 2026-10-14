package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card baselines by code. */
interface RateCardBaselineStore {
    fun find(code: String): RateCardBaseline?
    fun save(item: RateCardBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardBaselineStore(
    var items: MutableMap<String, RateCardBaseline>,
    var order: MutableList<String>,
) : RateCardBaselineStore {
    override fun find(code: String): RateCardBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardBaseline) {
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

    override fun list(): MutableList<RateCardBaseline> {
        val out = mutableListOf<RateCardBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardBaselineStore(): RateCardBaselineStore {
    return MemoryRateCardBaselineStore(mutableMapOf<String, RateCardBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardBaselineStore(
    var inner: RateCardBaselineStore,
    var cache: TtlCache<RateCardBaseline>,
    var hits: Int,
    var misses: Int,
) : RateCardBaselineStore {
    override fun find(code: String): RateCardBaseline? {
        val hit = (cache.get(code) as RateCardBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardBaseline> = inner.list()
}

fun newCachedRateCardBaselineStore(inner: RateCardBaselineStore, ttl: Int, clock: Clock): RateCardBaselineStore {
    return CachedRateCardBaselineStore(
        inner = inner,
        cache = newTtlCache<RateCardBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card baselines. Empty fields match all. */
data class RateCardBaselineQuery(
    var label: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardBaseline): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<RateCardBaseline>): MutableList<RateCardBaseline> {
        val matched = filter(items) { candidate: RateCardBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardBaselines(store: RateCardBaselineStore, query: RateCardBaselineQuery): MutableList<RateCardBaseline> {
    return query.apply(store.list())
}
