package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global rate card bundles by code. */
interface GlobalRateCardBundleStore {
    fun find(code: String): GlobalRateCardBundle?
    fun save(item: GlobalRateCardBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalRateCardBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalRateCardBundleStore(
    var items: MutableMap<String, GlobalRateCardBundle>,
    var order: MutableList<String>,
) : GlobalRateCardBundleStore {
    override fun find(code: String): GlobalRateCardBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalRateCardBundle) {
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

    override fun list(): MutableList<GlobalRateCardBundle> {
        val out = mutableListOf<GlobalRateCardBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalRateCardBundleStore(): GlobalRateCardBundleStore {
    return MemoryGlobalRateCardBundleStore(
        mutableMapOf<String, GlobalRateCardBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalRateCardBundleStore(
    var inner: GlobalRateCardBundleStore,
    var cache: TtlCache<GlobalRateCardBundle>,
    var hits: Int,
    var misses: Int,
) : GlobalRateCardBundleStore {
    override fun find(code: String): GlobalRateCardBundle? {
        val hit = (cache.get(code) as GlobalRateCardBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalRateCardBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalRateCardBundle> = inner.list()
}

fun newCachedGlobalRateCardBundleStore(inner: GlobalRateCardBundleStore, ttl: Int, clock: Clock): GlobalRateCardBundleStore {
    return CachedGlobalRateCardBundleStore(
        inner = inner,
        cache = newTtlCache<GlobalRateCardBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global rate card bundles. Empty fields match all. */
data class GlobalRateCardBundleQuery(
    var channel: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalRateCardBundle): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<GlobalRateCardBundle>): MutableList<GlobalRateCardBundle> {
        val matched = filter(items) { candidate: GlobalRateCardBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalRateCardBundles(store: GlobalRateCardBundleStore, query: GlobalRateCardBundleQuery): MutableList<GlobalRateCardBundle> {
    return query.apply(store.list())
}
