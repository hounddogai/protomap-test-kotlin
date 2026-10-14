package com.acme.transport.fleetops.tire.preference

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire preferences by code. */
interface TirePreferenceStore {
    fun find(code: String): TirePreference?
    fun save(item: TirePreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<TirePreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTirePreferenceStore(
    var items: MutableMap<String, TirePreference>,
    var order: MutableList<String>,
) : TirePreferenceStore {
    override fun find(code: String): TirePreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TirePreference) {
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

    override fun list(): MutableList<TirePreference> {
        val out = mutableListOf<TirePreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTirePreferenceStore(): TirePreferenceStore {
    return MemoryTirePreferenceStore(mutableMapOf<String, TirePreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTirePreferenceStore(
    var inner: TirePreferenceStore,
    var cache: TtlCache<TirePreference>,
    var hits: Int,
    var misses: Int,
) : TirePreferenceStore {
    override fun find(code: String): TirePreference? {
        val hit = (cache.get(code) as TirePreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TirePreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TirePreference> = inner.list()
}

fun newCachedTirePreferenceStore(inner: TirePreferenceStore, ttl: Int, clock: Clock): TirePreferenceStore {
    return CachedTirePreferenceStore(
        inner = inner,
        cache = newTtlCache<TirePreference>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire preferences. Empty fields match all. */
data class TirePreferenceQuery(
    var label: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TirePreference): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TirePreference>): MutableList<TirePreference> {
        val matched = filter(items) { candidate: TirePreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTirePreferences(store: TirePreferenceStore, query: TirePreferenceQuery): MutableList<TirePreference> {
    return query.apply(store.list())
}
