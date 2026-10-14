package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped lane plan preferences by code. */
interface CappedLanePlanPreferenceStore {
    fun find(code: String): CappedLanePlanPreference?
    fun save(item: CappedLanePlanPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedLanePlanPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedLanePlanPreferenceStore(
    var items: MutableMap<String, CappedLanePlanPreference>,
    var order: MutableList<String>,
) : CappedLanePlanPreferenceStore {
    override fun find(code: String): CappedLanePlanPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedLanePlanPreference) {
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

    override fun list(): MutableList<CappedLanePlanPreference> {
        val out = mutableListOf<CappedLanePlanPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedLanePlanPreferenceStore(): CappedLanePlanPreferenceStore {
    return MemoryCappedLanePlanPreferenceStore(
        mutableMapOf<String, CappedLanePlanPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedLanePlanPreferenceStore(
    var inner: CappedLanePlanPreferenceStore,
    var cache: TtlCache<CappedLanePlanPreference>,
    var hits: Int,
    var misses: Int,
) : CappedLanePlanPreferenceStore {
    override fun find(code: String): CappedLanePlanPreference? {
        val hit = (cache.get(code) as CappedLanePlanPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedLanePlanPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedLanePlanPreference> = inner.list()
}

fun newCachedCappedLanePlanPreferenceStore(inner: CappedLanePlanPreferenceStore, ttl: Int, clock: Clock): CappedLanePlanPreferenceStore {
    return CachedCappedLanePlanPreferenceStore(
        inner = inner,
        cache = newTtlCache<CappedLanePlanPreference>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped lane plan preferences. Empty fields match all. */
data class CappedLanePlanPreferenceQuery(
    var lane: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedLanePlanPreference): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedLanePlanPreference>): MutableList<CappedLanePlanPreference> {
        val matched = filter(items) { candidate: CappedLanePlanPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedLanePlanPreferences(store: CappedLanePlanPreferenceStore, query: CappedLanePlanPreferenceQuery): MutableList<CappedLanePlanPreference> {
    return query.apply(store.list())
}
