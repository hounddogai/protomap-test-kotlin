package com.acme.transport.freightpay.fuelsurcharge.preference

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge preferences by code. */
interface FuelSurchargePreferenceStore {
    fun find(code: String): FuelSurchargePreference?
    fun save(item: FuelSurchargePreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargePreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargePreferenceStore(
    var items: MutableMap<String, FuelSurchargePreference>,
    var order: MutableList<String>,
) : FuelSurchargePreferenceStore {
    override fun find(code: String): FuelSurchargePreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargePreference) {
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

    override fun list(): MutableList<FuelSurchargePreference> {
        val out = mutableListOf<FuelSurchargePreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargePreferenceStore(): FuelSurchargePreferenceStore {
    return MemoryFuelSurchargePreferenceStore(
        mutableMapOf<String, FuelSurchargePreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargePreferenceStore(
    var inner: FuelSurchargePreferenceStore,
    var cache: TtlCache<FuelSurchargePreference>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargePreferenceStore {
    override fun find(code: String): FuelSurchargePreference? {
        val hit = (cache.get(code) as FuelSurchargePreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargePreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargePreference> = inner.list()
}

fun newCachedFuelSurchargePreferenceStore(inner: FuelSurchargePreferenceStore, ttl: Int, clock: Clock): FuelSurchargePreferenceStore {
    return CachedFuelSurchargePreferenceStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargePreference>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge preferences. Empty fields match all. */
data class FuelSurchargePreferenceQuery(
    var title: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargePreference): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<FuelSurchargePreference>): MutableList<FuelSurchargePreference> {
        val matched = filter(items) { candidate: FuelSurchargePreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargePreferences(store: FuelSurchargePreferenceStore, query: FuelSurchargePreferenceQuery): MutableList<FuelSurchargePreference> {
    return query.apply(store.list())
}
