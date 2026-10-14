package com.acme.transport.freightpay.ratecard.bundle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card bundles by code. */
interface RateCardBundleStore {
    fun find(code: String): RateCardBundle?
    fun save(item: RateCardBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardBundleStore(
    var items: MutableMap<String, RateCardBundle>,
    var order: MutableList<String>,
) : RateCardBundleStore {
    override fun find(code: String): RateCardBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardBundle) {
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

    override fun list(): MutableList<RateCardBundle> {
        val out = mutableListOf<RateCardBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardBundleStore(): RateCardBundleStore {
    return MemoryRateCardBundleStore(mutableMapOf<String, RateCardBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardBundleStore(
    var inner: RateCardBundleStore,
    var cache: TtlCache<RateCardBundle>,
    var hits: Int,
    var misses: Int,
) : RateCardBundleStore {
    override fun find(code: String): RateCardBundle? {
        val hit = (cache.get(code) as RateCardBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardBundle> = inner.list()
}

fun newCachedRateCardBundleStore(inner: RateCardBundleStore, ttl: Int, clock: Clock): RateCardBundleStore {
    return CachedRateCardBundleStore(
        inner = inner,
        cache = newTtlCache<RateCardBundle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card bundles. Empty fields match all. */
data class RateCardBundleQuery(
    var category: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardBundle): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardBundle>): MutableList<RateCardBundle> {
        val matched = filter(items) { candidate: RateCardBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardBundles(store: RateCardBundleStore, query: RateCardBundleQuery): MutableList<RateCardBundle> {
    return query.apply(store.list())
}
