package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient fuel surcharge preferences by code. */
interface LenientFuelSurchargePreferenceStore {
    fun find(code: String): LenientFuelSurchargePreference?
    fun save(item: LenientFuelSurchargePreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientFuelSurchargePreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientFuelSurchargePreferenceStore(
    var items: MutableMap<String, LenientFuelSurchargePreference>,
    var order: MutableList<String>,
) : LenientFuelSurchargePreferenceStore {
    override fun find(code: String): LenientFuelSurchargePreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientFuelSurchargePreference) {
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

    override fun list(): MutableList<LenientFuelSurchargePreference> {
        val out = mutableListOf<LenientFuelSurchargePreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientFuelSurchargePreferenceStore(): LenientFuelSurchargePreferenceStore {
    return MemoryLenientFuelSurchargePreferenceStore(
        mutableMapOf<String, LenientFuelSurchargePreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientFuelSurchargePreferenceStore(
    var inner: LenientFuelSurchargePreferenceStore,
    var cache: TtlCache<LenientFuelSurchargePreference>,
    var hits: Int,
    var misses: Int,
) : LenientFuelSurchargePreferenceStore {
    override fun find(code: String): LenientFuelSurchargePreference? {
        val hit = (cache.get(code) as LenientFuelSurchargePreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientFuelSurchargePreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientFuelSurchargePreference> = inner.list()
}

fun newCachedLenientFuelSurchargePreferenceStore(inner: LenientFuelSurchargePreferenceStore, ttl: Int, clock: Clock): LenientFuelSurchargePreferenceStore {
    return CachedLenientFuelSurchargePreferenceStore(
        inner = inner,
        cache = newTtlCache<LenientFuelSurchargePreference>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient fuel surcharge preferences. Empty fields match all. */
data class LenientFuelSurchargePreferenceQuery(
    var depot: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientFuelSurchargePreference): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientFuelSurchargePreference>): MutableList<LenientFuelSurchargePreference> {
        val matched = filter(items) { candidate: LenientFuelSurchargePreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientFuelSurchargePreferences(store: LenientFuelSurchargePreferenceStore, query: LenientFuelSurchargePreferenceQuery): MutableList<LenientFuelSurchargePreference> {
    return query.apply(store.list())
}
