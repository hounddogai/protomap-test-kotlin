package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual rate card preferences by code. */
interface ManualRateCardPreferenceStore {
    fun find(code: String): ManualRateCardPreference?
    fun save(item: ManualRateCardPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualRateCardPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualRateCardPreferenceStore(
    var items: MutableMap<String, ManualRateCardPreference>,
    var order: MutableList<String>,
) : ManualRateCardPreferenceStore {
    override fun find(code: String): ManualRateCardPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualRateCardPreference) {
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

    override fun list(): MutableList<ManualRateCardPreference> {
        val out = mutableListOf<ManualRateCardPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualRateCardPreferenceStore(): ManualRateCardPreferenceStore {
    return MemoryManualRateCardPreferenceStore(
        mutableMapOf<String, ManualRateCardPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualRateCardPreferenceStore(
    var inner: ManualRateCardPreferenceStore,
    var cache: TtlCache<ManualRateCardPreference>,
    var hits: Int,
    var misses: Int,
) : ManualRateCardPreferenceStore {
    override fun find(code: String): ManualRateCardPreference? {
        val hit = (cache.get(code) as ManualRateCardPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualRateCardPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualRateCardPreference> = inner.list()
}

fun newCachedManualRateCardPreferenceStore(inner: ManualRateCardPreferenceStore, ttl: Int, clock: Clock): ManualRateCardPreferenceStore {
    return CachedManualRateCardPreferenceStore(
        inner = inner,
        cache = newTtlCache<ManualRateCardPreference>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual rate card preferences. Empty fields match all. */
data class ManualRateCardPreferenceQuery(
    var depot: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualRateCardPreference): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualRateCardPreference>): MutableList<ManualRateCardPreference> {
        val matched = filter(items) { candidate: ManualRateCardPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualRateCardPreferences(store: ManualRateCardPreferenceStore, query: ManualRateCardPreferenceQuery): MutableList<ManualRateCardPreference> {
    return query.apply(store.list())
}
