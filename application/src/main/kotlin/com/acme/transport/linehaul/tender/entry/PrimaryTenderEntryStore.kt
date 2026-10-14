package com.acme.transport.linehaul.tender.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps primary tender entries by code. */
interface PrimaryTenderEntryStore {
    fun find(code: String): PrimaryTenderEntry?
    fun save(item: PrimaryTenderEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTenderEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTenderEntryStore(
    var items: MutableMap<String, PrimaryTenderEntry>,
    var order: MutableList<String>,
) : PrimaryTenderEntryStore {
    override fun find(code: String): PrimaryTenderEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTenderEntry) {
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

    override fun list(): MutableList<PrimaryTenderEntry> {
        val out = mutableListOf<PrimaryTenderEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTenderEntryStore(): PrimaryTenderEntryStore {
    return MemoryPrimaryTenderEntryStore(mutableMapOf<String, PrimaryTenderEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTenderEntryStore(
    var inner: PrimaryTenderEntryStore,
    var cache: TtlCache<PrimaryTenderEntry>,
    var hits: Int,
    var misses: Int,
) : PrimaryTenderEntryStore {
    override fun find(code: String): PrimaryTenderEntry? {
        val hit = (cache.get(code) as PrimaryTenderEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTenderEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTenderEntry> = inner.list()
}

fun newCachedPrimaryTenderEntryStore(inner: PrimaryTenderEntryStore, ttl: Int, clock: Clock): PrimaryTenderEntryStore {
    return CachedPrimaryTenderEntryStore(
        inner = inner,
        cache = newTtlCache<PrimaryTenderEntry>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary tender entries. Empty fields match all. */
data class PrimaryTenderEntryQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTenderEntry): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTenderEntry>): MutableList<PrimaryTenderEntry> {
        val matched = filter(items) { candidate: PrimaryTenderEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTenderEntries(store: PrimaryTenderEntryStore, query: PrimaryTenderEntryQuery): MutableList<PrimaryTenderEntry> {
    return query.apply(store.list())
}
