package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal fuel surcharge preferences by code. */
interface SeasonalFuelSurchargePreferenceStore {
    fun find(code: String): SeasonalFuelSurchargePreference?
    fun save(item: SeasonalFuelSurchargePreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalFuelSurchargePreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalFuelSurchargePreferenceStore(
    var items: MutableMap<String, SeasonalFuelSurchargePreference>,
    var order: MutableList<String>,
) : SeasonalFuelSurchargePreferenceStore {
    override fun find(code: String): SeasonalFuelSurchargePreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalFuelSurchargePreference) {
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

    override fun list(): MutableList<SeasonalFuelSurchargePreference> {
        val out = mutableListOf<SeasonalFuelSurchargePreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalFuelSurchargePreferenceStore(): SeasonalFuelSurchargePreferenceStore {
    return MemorySeasonalFuelSurchargePreferenceStore(
        mutableMapOf<String, SeasonalFuelSurchargePreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalFuelSurchargePreferenceStore(
    var inner: SeasonalFuelSurchargePreferenceStore,
    var cache: TtlCache<SeasonalFuelSurchargePreference>,
    var hits: Int,
    var misses: Int,
) : SeasonalFuelSurchargePreferenceStore {
    override fun find(code: String): SeasonalFuelSurchargePreference? {
        val hit = (cache.get(code) as SeasonalFuelSurchargePreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalFuelSurchargePreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalFuelSurchargePreference> = inner.list()
}

fun newCachedSeasonalFuelSurchargePreferenceStore(inner: SeasonalFuelSurchargePreferenceStore, ttl: Int, clock: Clock): SeasonalFuelSurchargePreferenceStore {
    return CachedSeasonalFuelSurchargePreferenceStore(
        inner = inner,
        cache = newTtlCache<SeasonalFuelSurchargePreference>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal fuel surcharge preferences. Empty fields match all. */
data class SeasonalFuelSurchargePreferenceQuery(
    var label: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalFuelSurchargePreference): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalFuelSurchargePreference>): MutableList<SeasonalFuelSurchargePreference> {
        val matched = filter(items) { candidate: SeasonalFuelSurchargePreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalFuelSurchargePreferences(store: SeasonalFuelSurchargePreferenceStore, query: SeasonalFuelSurchargePreferenceQuery): MutableList<SeasonalFuelSurchargePreference> {
    return query.apply(store.list())
}
