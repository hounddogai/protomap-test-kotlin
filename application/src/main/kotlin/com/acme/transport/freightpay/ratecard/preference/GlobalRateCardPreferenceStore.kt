package com.acme.transport.freightpay.ratecard.preference

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global rate card preferences by code. */
interface GlobalRateCardPreferenceStore {
    fun find(code: String): GlobalRateCardPreference?
    fun save(item: GlobalRateCardPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalRateCardPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalRateCardPreferenceStore(
    var items: MutableMap<String, GlobalRateCardPreference>,
    var order: MutableList<String>,
) : GlobalRateCardPreferenceStore {
    override fun find(code: String): GlobalRateCardPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalRateCardPreference) {
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

    override fun list(): MutableList<GlobalRateCardPreference> {
        val out = mutableListOf<GlobalRateCardPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalRateCardPreferenceStore(): GlobalRateCardPreferenceStore {
    return MemoryGlobalRateCardPreferenceStore(
        mutableMapOf<String, GlobalRateCardPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalRateCardPreferenceStore(
    var inner: GlobalRateCardPreferenceStore,
    var cache: TtlCache<GlobalRateCardPreference>,
    var hits: Int,
    var misses: Int,
) : GlobalRateCardPreferenceStore {
    override fun find(code: String): GlobalRateCardPreference? {
        val hit = (cache.get(code) as GlobalRateCardPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalRateCardPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalRateCardPreference> = inner.list()
}

fun newCachedGlobalRateCardPreferenceStore(inner: GlobalRateCardPreferenceStore, ttl: Int, clock: Clock): GlobalRateCardPreferenceStore {
    return CachedGlobalRateCardPreferenceStore(
        inner = inner,
        cache = newTtlCache<GlobalRateCardPreference>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global rate card preferences. Empty fields match all. */
data class GlobalRateCardPreferenceQuery(
    var country: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalRateCardPreference): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<GlobalRateCardPreference>): MutableList<GlobalRateCardPreference> {
        val matched = filter(items) { candidate: GlobalRateCardPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalRateCardPreferences(store: GlobalRateCardPreferenceStore, query: GlobalRateCardPreferenceQuery): MutableList<GlobalRateCardPreference> {
    return query.apply(store.list())
}
