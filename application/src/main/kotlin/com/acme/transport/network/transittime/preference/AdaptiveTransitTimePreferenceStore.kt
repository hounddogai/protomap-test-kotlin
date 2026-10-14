package com.acme.transport.network.transittime.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps adaptive transit time preferences by code. */
interface AdaptiveTransitTimePreferenceStore {
    fun find(code: String): AdaptiveTransitTimePreference?
    fun save(item: AdaptiveTransitTimePreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveTransitTimePreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveTransitTimePreferenceStore(
    var items: MutableMap<String, AdaptiveTransitTimePreference>,
    var order: MutableList<String>,
) : AdaptiveTransitTimePreferenceStore {
    override fun find(code: String): AdaptiveTransitTimePreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveTransitTimePreference) {
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

    override fun list(): MutableList<AdaptiveTransitTimePreference> {
        val out = mutableListOf<AdaptiveTransitTimePreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveTransitTimePreferenceStore(): AdaptiveTransitTimePreferenceStore {
    return MemoryAdaptiveTransitTimePreferenceStore(
        mutableMapOf<String, AdaptiveTransitTimePreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveTransitTimePreferenceStore(
    var inner: AdaptiveTransitTimePreferenceStore,
    var cache: TtlCache<AdaptiveTransitTimePreference>,
    var hits: Int,
    var misses: Int,
) : AdaptiveTransitTimePreferenceStore {
    override fun find(code: String): AdaptiveTransitTimePreference? {
        val hit = (cache.get(code) as AdaptiveTransitTimePreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveTransitTimePreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveTransitTimePreference> = inner.list()
}

fun newCachedAdaptiveTransitTimePreferenceStore(inner: AdaptiveTransitTimePreferenceStore, ttl: Int, clock: Clock): AdaptiveTransitTimePreferenceStore {
    return CachedAdaptiveTransitTimePreferenceStore(
        inner = inner,
        cache = newTtlCache<AdaptiveTransitTimePreference>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive transit time preferences. Empty fields match all. */
data class AdaptiveTransitTimePreferenceQuery(
    var source: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveTransitTimePreference): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveTransitTimePreference>): MutableList<AdaptiveTransitTimePreference> {
        val matched = filter(items) { candidate: AdaptiveTransitTimePreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveTransitTimePreferences(store: AdaptiveTransitTimePreferenceStore, query: AdaptiveTransitTimePreferenceQuery): MutableList<AdaptiveTransitTimePreference> {
    return query.apply(store.list())
}
