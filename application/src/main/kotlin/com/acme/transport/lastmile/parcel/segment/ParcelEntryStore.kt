package com.acme.transport.lastmile.parcel.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel entries by code. */
interface ParcelEntryStore {
    fun find(code: String): ParcelEntry?
    fun save(item: ParcelEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelEntryStore(
    var items: MutableMap<String, ParcelEntry>,
    var order: MutableList<String>,
) : ParcelEntryStore {
    override fun find(code: String): ParcelEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelEntry) {
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

    override fun list(): MutableList<ParcelEntry> {
        val out = mutableListOf<ParcelEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelEntryStore(): ParcelEntryStore {
    return MemoryParcelEntryStore(mutableMapOf<String, ParcelEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelEntryStore(
    var inner: ParcelEntryStore,
    var cache: TtlCache<ParcelEntry>,
    var hits: Int,
    var misses: Int,
) : ParcelEntryStore {
    override fun find(code: String): ParcelEntry? {
        val hit = (cache.get(code) as ParcelEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelEntry> = inner.list()
}

fun newCachedParcelEntryStore(inner: ParcelEntryStore, ttl: Int, clock: Clock): ParcelEntryStore {
    return CachedParcelEntryStore(
        inner = inner,
        cache = newTtlCache<ParcelEntry>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel entries. Empty fields match all. */
data class ParcelEntryQuery(
    var label: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelEntry): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelEntry>): MutableList<ParcelEntry> {
        val matched = filter(items) { candidate: ParcelEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelEntries(store: ParcelEntryStore, query: ParcelEntryQuery): MutableList<ParcelEntry> {
    return query.apply(store.list())
}
