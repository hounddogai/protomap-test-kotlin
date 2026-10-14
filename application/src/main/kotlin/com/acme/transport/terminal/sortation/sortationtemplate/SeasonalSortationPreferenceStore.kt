package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal sortation preferences by code. */
interface SeasonalSortationPreferenceStore {
    fun find(code: String): SeasonalSortationPreference?
    fun save(item: SeasonalSortationPreference)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalSortationPreference>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalSortationPreferenceStore(
    var items: MutableMap<String, SeasonalSortationPreference>,
    var order: MutableList<String>,
) : SeasonalSortationPreferenceStore {
    override fun find(code: String): SeasonalSortationPreference? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalSortationPreference) {
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

    override fun list(): MutableList<SeasonalSortationPreference> {
        val out = mutableListOf<SeasonalSortationPreference>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalSortationPreferenceStore(): SeasonalSortationPreferenceStore {
    return MemorySeasonalSortationPreferenceStore(
        mutableMapOf<String, SeasonalSortationPreference>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalSortationPreferenceStore(
    var inner: SeasonalSortationPreferenceStore,
    var cache: TtlCache<SeasonalSortationPreference>,
    var hits: Int,
    var misses: Int,
) : SeasonalSortationPreferenceStore {
    override fun find(code: String): SeasonalSortationPreference? {
        val hit = (cache.get(code) as SeasonalSortationPreference?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalSortationPreference) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalSortationPreference> = inner.list()
}

fun newCachedSeasonalSortationPreferenceStore(inner: SeasonalSortationPreferenceStore, ttl: Int, clock: Clock): SeasonalSortationPreferenceStore {
    return CachedSeasonalSortationPreferenceStore(
        inner = inner,
        cache = newTtlCache<SeasonalSortationPreference>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal sortation preferences. Empty fields match all. */
data class SeasonalSortationPreferenceQuery(
    var source: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalSortationPreference): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalSortationPreference>): MutableList<SeasonalSortationPreference> {
        val matched = filter(items) { candidate: SeasonalSortationPreference -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalSortationPreferences(store: SeasonalSortationPreferenceStore, query: SeasonalSortationPreferenceQuery): MutableList<SeasonalSortationPreference> {
    return query.apply(store.list())
}
