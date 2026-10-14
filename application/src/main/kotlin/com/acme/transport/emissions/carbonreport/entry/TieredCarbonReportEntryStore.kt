package com.acme.transport.emissions.carbonreport.entry

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered carbon report entries by code. */
interface TieredCarbonReportEntryStore {
    fun find(code: String): TieredCarbonReportEntry?
    fun save(item: TieredCarbonReportEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCarbonReportEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCarbonReportEntryStore(
    var items: MutableMap<String, TieredCarbonReportEntry>,
    var order: MutableList<String>,
) : TieredCarbonReportEntryStore {
    override fun find(code: String): TieredCarbonReportEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCarbonReportEntry) {
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

    override fun list(): MutableList<TieredCarbonReportEntry> {
        val out = mutableListOf<TieredCarbonReportEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCarbonReportEntryStore(): TieredCarbonReportEntryStore {
    return MemoryTieredCarbonReportEntryStore(
        mutableMapOf<String, TieredCarbonReportEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCarbonReportEntryStore(
    var inner: TieredCarbonReportEntryStore,
    var cache: TtlCache<TieredCarbonReportEntry>,
    var hits: Int,
    var misses: Int,
) : TieredCarbonReportEntryStore {
    override fun find(code: String): TieredCarbonReportEntry? {
        val hit = (cache.get(code) as TieredCarbonReportEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCarbonReportEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCarbonReportEntry> = inner.list()
}

fun newCachedTieredCarbonReportEntryStore(inner: TieredCarbonReportEntryStore, ttl: Int, clock: Clock): TieredCarbonReportEntryStore {
    return CachedTieredCarbonReportEntryStore(
        inner = inner,
        cache = newTtlCache<TieredCarbonReportEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered carbon report entries. Empty fields match all. */
data class TieredCarbonReportEntryQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCarbonReportEntry): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredCarbonReportEntry>): MutableList<TieredCarbonReportEntry> {
        val matched = filter(items) { candidate: TieredCarbonReportEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCarbonReportEntries(store: TieredCarbonReportEntryStore, query: TieredCarbonReportEntryQuery): MutableList<TieredCarbonReportEntry> {
    return query.apply(store.list())
}
