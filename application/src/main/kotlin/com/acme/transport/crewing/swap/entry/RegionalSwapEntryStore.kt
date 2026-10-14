package com.acme.transport.crewing.swap.entry

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional swap entries by code. */
interface RegionalSwapEntryStore {
    fun find(code: String): RegionalSwapEntry?
    fun save(item: RegionalSwapEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalSwapEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalSwapEntryStore(
    var items: MutableMap<String, RegionalSwapEntry>,
    var order: MutableList<String>,
) : RegionalSwapEntryStore {
    override fun find(code: String): RegionalSwapEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalSwapEntry) {
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

    override fun list(): MutableList<RegionalSwapEntry> {
        val out = mutableListOf<RegionalSwapEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalSwapEntryStore(): RegionalSwapEntryStore {
    return MemoryRegionalSwapEntryStore(mutableMapOf<String, RegionalSwapEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalSwapEntryStore(
    var inner: RegionalSwapEntryStore,
    var cache: TtlCache<RegionalSwapEntry>,
    var hits: Int,
    var misses: Int,
) : RegionalSwapEntryStore {
    override fun find(code: String): RegionalSwapEntry? {
        val hit = (cache.get(code) as RegionalSwapEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalSwapEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalSwapEntry> = inner.list()
}

fun newCachedRegionalSwapEntryStore(inner: RegionalSwapEntryStore, ttl: Int, clock: Clock): RegionalSwapEntryStore {
    return CachedRegionalSwapEntryStore(
        inner = inner,
        cache = newTtlCache<RegionalSwapEntry>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional swap entries. Empty fields match all. */
data class RegionalSwapEntryQuery(
    var unit: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalSwapEntry): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalSwapEntry>): MutableList<RegionalSwapEntry> {
        val matched = filter(items) { candidate: RegionalSwapEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalSwapEntries(store: RegionalSwapEntryStore, query: RegionalSwapEntryQuery): MutableList<RegionalSwapEntry> {
    return query.apply(store.list())
}
