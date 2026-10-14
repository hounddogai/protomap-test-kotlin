package com.acme.transport.customs.inspection.inspectionentry

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped inspection entries by code. */
interface CappedInspectionEntryStore {
    fun find(code: String): CappedInspectionEntry?
    fun save(item: CappedInspectionEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedInspectionEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedInspectionEntryStore(
    var items: MutableMap<String, CappedInspectionEntry>,
    var order: MutableList<String>,
) : CappedInspectionEntryStore {
    override fun find(code: String): CappedInspectionEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedInspectionEntry) {
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

    override fun list(): MutableList<CappedInspectionEntry> {
        val out = mutableListOf<CappedInspectionEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedInspectionEntryStore(): CappedInspectionEntryStore {
    return MemoryCappedInspectionEntryStore(
        mutableMapOf<String, CappedInspectionEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedInspectionEntryStore(
    var inner: CappedInspectionEntryStore,
    var cache: TtlCache<CappedInspectionEntry>,
    var hits: Int,
    var misses: Int,
) : CappedInspectionEntryStore {
    override fun find(code: String): CappedInspectionEntry? {
        val hit = (cache.get(code) as CappedInspectionEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedInspectionEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedInspectionEntry> = inner.list()
}

fun newCachedCappedInspectionEntryStore(inner: CappedInspectionEntryStore, ttl: Int, clock: Clock): CappedInspectionEntryStore {
    return CachedCappedInspectionEntryStore(
        inner = inner,
        cache = newTtlCache<CappedInspectionEntry>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped inspection entries. Empty fields match all. */
data class CappedInspectionEntryQuery(
    var category: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedInspectionEntry): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<CappedInspectionEntry>): MutableList<CappedInspectionEntry> {
        val matched = filter(items) { candidate: CappedInspectionEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedInspectionEntries(store: CappedInspectionEntryStore, query: CappedInspectionEntryQuery): MutableList<CappedInspectionEntry> {
    return query.apply(store.list())
}
