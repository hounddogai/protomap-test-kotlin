package com.acme.transport.linehaul.laneplan.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback lane plan preferences by code. */
interface FallbackLanePlanPreferenceStore {
    fun find(code: String): FallbackLanePlanPreference?
    fun save(item: FallbackLanePlanPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackLanePlanPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackLanePlanPreferenceStore(
    var items: MutableMap<String, FallbackLanePlanPreference>,
    var order: MutableList<String>,
) : FallbackLanePlanPreferenceStore {
    override fun find(code: String): FallbackLanePlanPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackLanePlanPreference) {
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

    override fun list(): MutableList<FallbackLanePlanPreference> {
        val out = mutableListOf<FallbackLanePlanPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackLanePlanPreferenceStore(): FallbackLanePlanPreferenceStore {
    return MemoryFallbackLanePlanPreferenceStore(
        mutableMapOf<String, FallbackLanePlanPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackLanePlanPreferenceStore(
    var inner: FallbackLanePlanPreferenceStore,
    var cache: TtlCache<FallbackLanePlanPreference>,
    var hits: Int,
    var misses: Int,
) : FallbackLanePlanPreferenceStore {
    override fun find(code: String): FallbackLanePlanPreference? {
        val hit = (cache.get(code) as FallbackLanePlanPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackLanePlanPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackLanePlanPreference> = inner.list()
}

fun newCachedFallbackLanePlanPreferenceStore(inner: FallbackLanePlanPreferenceStore, ttl: Int, clock: Clock): FallbackLanePlanPreferenceStore {
    return CachedFallbackLanePlanPreferenceStore(
        inner = inner,
        cache = newTtlCache<FallbackLanePlanPreference>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback lane plan preferences. Empty fields match all. */
data class FallbackLanePlanPreferenceQuery(
    var category: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackLanePlanPreference): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<FallbackLanePlanPreference>): MutableList<FallbackLanePlanPreference> {
        val matched = filter(items) { candidate: FallbackLanePlanPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackLanePlanPreferences(store: FallbackLanePlanPreferenceStore, query: FallbackLanePlanPreferenceQuery): MutableList<FallbackLanePlanPreference> {
    return query.apply(store.list())
}
