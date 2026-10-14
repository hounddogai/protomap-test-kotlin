package com.acme.transport.customs.inspection.entry

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection entries by code. */
interface InspectionEntryStore {
    fun find(code: String): InspectionEntry?
    fun save(item: InspectionEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionEntryStore(
    var items: MutableMap<String, InspectionEntry>,
    var order: MutableList<String>,
) : InspectionEntryStore {
    override fun find(code: String): InspectionEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionEntry) {
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

    override fun list(): MutableList<InspectionEntry> {
        val out = mutableListOf<InspectionEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionEntryStore(): InspectionEntryStore {
    return MemoryInspectionEntryStore(mutableMapOf<String, InspectionEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionEntryStore(
    var inner: InspectionEntryStore,
    var cache: TtlCache<InspectionEntry>,
    var hits: Int,
    var misses: Int,
) : InspectionEntryStore {
    override fun find(code: String): InspectionEntry? {
        val hit = (cache.get(code) as InspectionEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionEntry> = inner.list()
}

fun newCachedInspectionEntryStore(inner: InspectionEntryStore, ttl: Int, clock: Clock): InspectionEntryStore {
    return CachedInspectionEntryStore(
        inner = inner,
        cache = newTtlCache<InspectionEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection entries. Empty fields match all. */
data class InspectionEntryQuery(
    var locale: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionEntry): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionEntry>): MutableList<InspectionEntry> {
        val matched = filter(items) { candidate: InspectionEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionEntries(store: InspectionEntryStore, query: InspectionEntryQuery): MutableList<InspectionEntry> {
    return query.apply(store.list())
}
