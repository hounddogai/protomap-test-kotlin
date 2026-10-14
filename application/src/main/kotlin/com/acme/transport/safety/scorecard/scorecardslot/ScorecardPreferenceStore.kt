package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard preferences by code. */
interface ScorecardPreferenceStore {
    fun find(code: String): ScorecardPreference?
    fun save(item: ScorecardPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardPreferenceStore(
    var items: MutableMap<String, ScorecardPreference>,
    var order: MutableList<String>,
) : ScorecardPreferenceStore {
    override fun find(code: String): ScorecardPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardPreference) {
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

    override fun list(): MutableList<ScorecardPreference> {
        val out = mutableListOf<ScorecardPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardPreferenceStore(): ScorecardPreferenceStore {
    return MemoryScorecardPreferenceStore(mutableMapOf<String, ScorecardPreference>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardPreferenceStore(
    var inner: ScorecardPreferenceStore,
    var cache: TtlCache<ScorecardPreference>,
    var hits: Int,
    var misses: Int,
) : ScorecardPreferenceStore {
    override fun find(code: String): ScorecardPreference? {
        val hit = (cache.get(code) as ScorecardPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardPreference> = inner.list()
}

fun newCachedScorecardPreferenceStore(inner: ScorecardPreferenceStore, ttl: Int, clock: Clock): ScorecardPreferenceStore {
    return CachedScorecardPreferenceStore(
        inner = inner,
        cache = newTtlCache<ScorecardPreference>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard preferences. Empty fields match all. */
data class ScorecardPreferenceQuery(
    var locale: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardPreference): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<ScorecardPreference>): MutableList<ScorecardPreference> {
        val matched = filter(items) { candidate: ScorecardPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardPreferences(store: ScorecardPreferenceStore, query: ScorecardPreferenceQuery): MutableList<ScorecardPreference> {
    return query.apply(store.list())
}
