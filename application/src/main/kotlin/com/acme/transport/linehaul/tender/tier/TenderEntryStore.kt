package com.acme.transport.linehaul.tender.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender entries by code. */
interface TenderEntryStore {
    fun find(code: String): TenderEntry?
    fun save(item: TenderEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderEntryStore(
    var items: MutableMap<String, TenderEntry>,
    var order: MutableList<String>,
) : TenderEntryStore {
    override fun find(code: String): TenderEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderEntry) {
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

    override fun list(): MutableList<TenderEntry> {
        val out = mutableListOf<TenderEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderEntryStore(): TenderEntryStore {
    return MemoryTenderEntryStore(mutableMapOf<String, TenderEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderEntryStore(
    var inner: TenderEntryStore,
    var cache: TtlCache<TenderEntry>,
    var hits: Int,
    var misses: Int,
) : TenderEntryStore {
    override fun find(code: String): TenderEntry? {
        val hit = (cache.get(code) as TenderEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderEntry> = inner.list()
}

fun newCachedTenderEntryStore(inner: TenderEntryStore, ttl: Int, clock: Clock): TenderEntryStore {
    return CachedTenderEntryStore(
        inner = inner,
        cache = newTtlCache<TenderEntry>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender entries. Empty fields match all. */
data class TenderEntryQuery(
    var country: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderEntry): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TenderEntry>): MutableList<TenderEntry> {
        val matched = filter(items) { candidate: TenderEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderEntries(store: TenderEntryStore, query: TenderEntryQuery): MutableList<TenderEntry> {
    return query.apply(store.list())
}
