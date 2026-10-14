package com.acme.transport.freightpay.dispute.adaptivedisputeforecast

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered dispute preferences by code. */
interface TieredDisputePreferenceStore {
    fun find(code: String): TieredDisputePreference?
    fun save(item: TieredDisputePreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDisputePreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDisputePreferenceStore(
    var items: MutableMap<String, TieredDisputePreference>,
    var order: MutableList<String>,
) : TieredDisputePreferenceStore {
    override fun find(code: String): TieredDisputePreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDisputePreference) {
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

    override fun list(): MutableList<TieredDisputePreference> {
        val out = mutableListOf<TieredDisputePreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDisputePreferenceStore(): TieredDisputePreferenceStore {
    return MemoryTieredDisputePreferenceStore(
        mutableMapOf<String, TieredDisputePreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDisputePreferenceStore(
    var inner: TieredDisputePreferenceStore,
    var cache: TtlCache<TieredDisputePreference>,
    var hits: Int,
    var misses: Int,
) : TieredDisputePreferenceStore {
    override fun find(code: String): TieredDisputePreference? {
        val hit = (cache.get(code) as TieredDisputePreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDisputePreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDisputePreference> = inner.list()
}

fun newCachedTieredDisputePreferenceStore(inner: TieredDisputePreferenceStore, ttl: Int, clock: Clock): TieredDisputePreferenceStore {
    return CachedTieredDisputePreferenceStore(
        inner = inner,
        cache = newTtlCache<TieredDisputePreference>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered dispute preferences. Empty fields match all. */
data class TieredDisputePreferenceQuery(
    var country: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDisputePreference): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredDisputePreference>): MutableList<TieredDisputePreference> {
        val matched = filter(items) { candidate: TieredDisputePreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDisputePreferences(store: TieredDisputePreferenceStore, query: TieredDisputePreferenceQuery): MutableList<TieredDisputePreference> {
    return query.apply(store.list())
}
