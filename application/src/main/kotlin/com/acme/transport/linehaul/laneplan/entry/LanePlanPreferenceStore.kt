package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan preferences by code. */
interface LanePlanPreferenceStore {
    fun find(code: String): LanePlanPreference?
    fun save(item: LanePlanPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanPreferenceStore(
    var items: MutableMap<String, LanePlanPreference>,
    var order: MutableList<String>,
) : LanePlanPreferenceStore {
    override fun find(code: String): LanePlanPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanPreference) {
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

    override fun list(): MutableList<LanePlanPreference> {
        val out = mutableListOf<LanePlanPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanPreferenceStore(): LanePlanPreferenceStore {
    return MemoryLanePlanPreferenceStore(mutableMapOf<String, LanePlanPreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanPreferenceStore(
    var inner: LanePlanPreferenceStore,
    var cache: TtlCache<LanePlanPreference>,
    var hits: Int,
    var misses: Int,
) : LanePlanPreferenceStore {
    override fun find(code: String): LanePlanPreference? {
        val hit = (cache.get(code) as LanePlanPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanPreference> = inner.list()
}

fun newCachedLanePlanPreferenceStore(inner: LanePlanPreferenceStore, ttl: Int, clock: Clock): LanePlanPreferenceStore {
    return CachedLanePlanPreferenceStore(
        inner = inner,
        cache = newTtlCache<LanePlanPreference>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan preferences. Empty fields match all. */
data class LanePlanPreferenceQuery(
    var depot: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanPreference): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LanePlanPreference>): MutableList<LanePlanPreference> {
        val matched = filter(items) { candidate: LanePlanPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanPreferences(store: LanePlanPreferenceStore, query: LanePlanPreferenceQuery): MutableList<LanePlanPreference> {
    return query.apply(store.list())
}
