package com.acme.transport.network.transittime.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps seasonal transit time entries by code. */
interface SeasonalTransitTimeEntryStore {
    fun find(code: String): SeasonalTransitTimeEntry?
    fun save(item: SeasonalTransitTimeEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTransitTimeEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTransitTimeEntryStore(
    var items: MutableMap<String, SeasonalTransitTimeEntry>,
    var order: MutableList<String>,
) : SeasonalTransitTimeEntryStore {
    override fun find(code: String): SeasonalTransitTimeEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTransitTimeEntry) {
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

    override fun list(): MutableList<SeasonalTransitTimeEntry> {
        val out = mutableListOf<SeasonalTransitTimeEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTransitTimeEntryStore(): SeasonalTransitTimeEntryStore {
    return MemorySeasonalTransitTimeEntryStore(
        mutableMapOf<String, SeasonalTransitTimeEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTransitTimeEntryStore(
    var inner: SeasonalTransitTimeEntryStore,
    var cache: TtlCache<SeasonalTransitTimeEntry>,
    var hits: Int,
    var misses: Int,
) : SeasonalTransitTimeEntryStore {
    override fun find(code: String): SeasonalTransitTimeEntry? {
        val hit = (cache.get(code) as SeasonalTransitTimeEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTransitTimeEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTransitTimeEntry> = inner.list()
}

fun newCachedSeasonalTransitTimeEntryStore(inner: SeasonalTransitTimeEntryStore, ttl: Int, clock: Clock): SeasonalTransitTimeEntryStore {
    return CachedSeasonalTransitTimeEntryStore(
        inner = inner,
        cache = newTtlCache<SeasonalTransitTimeEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal transit time entries. Empty fields match all. */
data class SeasonalTransitTimeEntryQuery(
    var category: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTransitTimeEntry): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTransitTimeEntry>): MutableList<SeasonalTransitTimeEntry> {
        val matched = filter(items) { candidate: SeasonalTransitTimeEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTransitTimeEntries(store: SeasonalTransitTimeEntryStore, query: SeasonalTransitTimeEntryQuery): MutableList<SeasonalTransitTimeEntry> {
    return query.apply(store.list())
}
