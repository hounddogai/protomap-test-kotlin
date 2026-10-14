package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal defect entries by code. */
interface SeasonalDefectEntryStore {
    fun find(code: String): SeasonalDefectEntry?
    fun save(item: SeasonalDefectEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDefectEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDefectEntryStore(
    var items: MutableMap<String, SeasonalDefectEntry>,
    var order: MutableList<String>,
) : SeasonalDefectEntryStore {
    override fun find(code: String): SeasonalDefectEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDefectEntry) {
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

    override fun list(): MutableList<SeasonalDefectEntry> {
        val out = mutableListOf<SeasonalDefectEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDefectEntryStore(): SeasonalDefectEntryStore {
    return MemorySeasonalDefectEntryStore(mutableMapOf<String, SeasonalDefectEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDefectEntryStore(
    var inner: SeasonalDefectEntryStore,
    var cache: TtlCache<SeasonalDefectEntry>,
    var hits: Int,
    var misses: Int,
) : SeasonalDefectEntryStore {
    override fun find(code: String): SeasonalDefectEntry? {
        val hit = (cache.get(code) as SeasonalDefectEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDefectEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDefectEntry> = inner.list()
}

fun newCachedSeasonalDefectEntryStore(inner: SeasonalDefectEntryStore, ttl: Int, clock: Clock): SeasonalDefectEntryStore {
    return CachedSeasonalDefectEntryStore(
        inner = inner,
        cache = newTtlCache<SeasonalDefectEntry>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal defect entries. Empty fields match all. */
data class SeasonalDefectEntryQuery(
    var title: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDefectEntry): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<SeasonalDefectEntry>): MutableList<SeasonalDefectEntry> {
        val matched = filter(items) { candidate: SeasonalDefectEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDefectEntries(store: SeasonalDefectEntryStore, query: SeasonalDefectEntryQuery): MutableList<SeasonalDefectEntry> {
    return query.apply(store.list())
}
