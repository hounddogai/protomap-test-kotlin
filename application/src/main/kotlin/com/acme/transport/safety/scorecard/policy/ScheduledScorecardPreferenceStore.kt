package com.acme.transport.safety.scorecard.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scheduled scorecard preferences by code. */
interface ScheduledScorecardPreferenceStore {
    fun find(code: String): ScheduledScorecardPreference?
    fun save(item: ScheduledScorecardPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledScorecardPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledScorecardPreferenceStore(
    var items: MutableMap<String, ScheduledScorecardPreference>,
    var order: MutableList<String>,
) : ScheduledScorecardPreferenceStore {
    override fun find(code: String): ScheduledScorecardPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledScorecardPreference) {
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

    override fun list(): MutableList<ScheduledScorecardPreference> {
        val out = mutableListOf<ScheduledScorecardPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledScorecardPreferenceStore(): ScheduledScorecardPreferenceStore {
    return MemoryScheduledScorecardPreferenceStore(
        mutableMapOf<String, ScheduledScorecardPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledScorecardPreferenceStore(
    var inner: ScheduledScorecardPreferenceStore,
    var cache: TtlCache<ScheduledScorecardPreference>,
    var hits: Int,
    var misses: Int,
) : ScheduledScorecardPreferenceStore {
    override fun find(code: String): ScheduledScorecardPreference? {
        val hit = (cache.get(code) as ScheduledScorecardPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledScorecardPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledScorecardPreference> = inner.list()
}

fun newCachedScheduledScorecardPreferenceStore(inner: ScheduledScorecardPreferenceStore, ttl: Int, clock: Clock): ScheduledScorecardPreferenceStore {
    return CachedScheduledScorecardPreferenceStore(
        inner = inner,
        cache = newTtlCache<ScheduledScorecardPreference>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled scorecard preferences. Empty fields match all. */
data class ScheduledScorecardPreferenceQuery(
    var label: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledScorecardPreference): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledScorecardPreference>): MutableList<ScheduledScorecardPreference> {
        val matched = filter(items) { candidate: ScheduledScorecardPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledScorecardPreferences(store: ScheduledScorecardPreferenceStore, query: ScheduledScorecardPreferenceQuery): MutableList<ScheduledScorecardPreference> {
    return query.apply(store.list())
}
