package com.acme.transport.crewing.restbreak.proposal

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional rest break entries by code. */
interface RegionalRestBreakEntryStore {
    fun find(code: String): RegionalRestBreakEntry?
    fun save(item: RegionalRestBreakEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalRestBreakEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalRestBreakEntryStore(
    var items: MutableMap<String, RegionalRestBreakEntry>,
    var order: MutableList<String>,
) : RegionalRestBreakEntryStore {
    override fun find(code: String): RegionalRestBreakEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalRestBreakEntry) {
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

    override fun list(): MutableList<RegionalRestBreakEntry> {
        val out = mutableListOf<RegionalRestBreakEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalRestBreakEntryStore(): RegionalRestBreakEntryStore {
    return MemoryRegionalRestBreakEntryStore(
        mutableMapOf<String, RegionalRestBreakEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalRestBreakEntryStore(
    var inner: RegionalRestBreakEntryStore,
    var cache: TtlCache<RegionalRestBreakEntry>,
    var hits: Int,
    var misses: Int,
) : RegionalRestBreakEntryStore {
    override fun find(code: String): RegionalRestBreakEntry? {
        val hit = (cache.get(code) as RegionalRestBreakEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalRestBreakEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalRestBreakEntry> = inner.list()
}

fun newCachedRegionalRestBreakEntryStore(inner: RegionalRestBreakEntryStore, ttl: Int, clock: Clock): RegionalRestBreakEntryStore {
    return CachedRegionalRestBreakEntryStore(
        inner = inner,
        cache = newTtlCache<RegionalRestBreakEntry>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional rest break entries. Empty fields match all. */
data class RegionalRestBreakEntryQuery(
    var lane: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalRestBreakEntry): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalRestBreakEntry>): MutableList<RegionalRestBreakEntry> {
        val matched = filter(items) { candidate: RegionalRestBreakEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalRestBreakEntries(store: RegionalRestBreakEntryStore, query: RegionalRestBreakEntryQuery): MutableList<RegionalRestBreakEntry> {
    return query.apply(store.list())
}
