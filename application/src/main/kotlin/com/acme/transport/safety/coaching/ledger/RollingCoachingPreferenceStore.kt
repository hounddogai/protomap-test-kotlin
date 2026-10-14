package com.acme.transport.safety.coaching.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling coaching preferences by code. */
interface RollingCoachingPreferenceStore {
    fun find(code: String): RollingCoachingPreference?
    fun save(item: RollingCoachingPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingCoachingPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingCoachingPreferenceStore(
    var items: MutableMap<String, RollingCoachingPreference>,
    var order: MutableList<String>,
) : RollingCoachingPreferenceStore {
    override fun find(code: String): RollingCoachingPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingCoachingPreference) {
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

    override fun list(): MutableList<RollingCoachingPreference> {
        val out = mutableListOf<RollingCoachingPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingCoachingPreferenceStore(): RollingCoachingPreferenceStore {
    return MemoryRollingCoachingPreferenceStore(
        mutableMapOf<String, RollingCoachingPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingCoachingPreferenceStore(
    var inner: RollingCoachingPreferenceStore,
    var cache: TtlCache<RollingCoachingPreference>,
    var hits: Int,
    var misses: Int,
) : RollingCoachingPreferenceStore {
    override fun find(code: String): RollingCoachingPreference? {
        val hit = (cache.get(code) as RollingCoachingPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingCoachingPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingCoachingPreference> = inner.list()
}

fun newCachedRollingCoachingPreferenceStore(inner: RollingCoachingPreferenceStore, ttl: Int, clock: Clock): RollingCoachingPreferenceStore {
    return CachedRollingCoachingPreferenceStore(
        inner = inner,
        cache = newTtlCache<RollingCoachingPreference>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling coaching preferences. Empty fields match all. */
data class RollingCoachingPreferenceQuery(
    var title: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingCoachingPreference): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingCoachingPreference>): MutableList<RollingCoachingPreference> {
        val matched = filter(items) { candidate: RollingCoachingPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingCoachingPreferences(store: RollingCoachingPreferenceStore, query: RollingCoachingPreferenceQuery): MutableList<RollingCoachingPreference> {
    return query.apply(store.list())
}
