package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled emission factor preferences by code. */
interface ScheduledEmissionFactorPreferenceStore {
    fun find(code: String): ScheduledEmissionFactorPreference?
    fun save(item: ScheduledEmissionFactorPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledEmissionFactorPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledEmissionFactorPreferenceStore(
    var items: MutableMap<String, ScheduledEmissionFactorPreference>,
    var order: MutableList<String>,
) : ScheduledEmissionFactorPreferenceStore {
    override fun find(code: String): ScheduledEmissionFactorPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledEmissionFactorPreference) {
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

    override fun list(): MutableList<ScheduledEmissionFactorPreference> {
        val out = mutableListOf<ScheduledEmissionFactorPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledEmissionFactorPreferenceStore(): ScheduledEmissionFactorPreferenceStore {
    return MemoryScheduledEmissionFactorPreferenceStore(
        mutableMapOf<String, ScheduledEmissionFactorPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledEmissionFactorPreferenceStore(
    var inner: ScheduledEmissionFactorPreferenceStore,
    var cache: TtlCache<ScheduledEmissionFactorPreference>,
    var hits: Int,
    var misses: Int,
) : ScheduledEmissionFactorPreferenceStore {
    override fun find(code: String): ScheduledEmissionFactorPreference? {
        val hit = (cache.get(code) as ScheduledEmissionFactorPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledEmissionFactorPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledEmissionFactorPreference> = inner.list()
}

fun newCachedScheduledEmissionFactorPreferenceStore(inner: ScheduledEmissionFactorPreferenceStore, ttl: Int, clock: Clock): ScheduledEmissionFactorPreferenceStore {
    return CachedScheduledEmissionFactorPreferenceStore(
        inner = inner,
        cache = newTtlCache<ScheduledEmissionFactorPreference>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled emission factor preferences. Empty fields match all. */
data class ScheduledEmissionFactorPreferenceQuery(
    var segment: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledEmissionFactorPreference): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledEmissionFactorPreference>): MutableList<ScheduledEmissionFactorPreference> {
        val matched = filter(items) { candidate: ScheduledEmissionFactorPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledEmissionFactorPreferences(store: ScheduledEmissionFactorPreferenceStore, query: ScheduledEmissionFactorPreferenceQuery): MutableList<ScheduledEmissionFactorPreference> {
    return query.apply(store.list())
}
