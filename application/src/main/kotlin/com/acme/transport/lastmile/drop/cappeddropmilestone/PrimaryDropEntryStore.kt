package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps primary drop entries by code. */
interface PrimaryDropEntryStore {
    fun find(code: String): PrimaryDropEntry?
    fun save(item: PrimaryDropEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryDropEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryDropEntryStore(
    var items: MutableMap<String, PrimaryDropEntry>,
    var order: MutableList<String>,
) : PrimaryDropEntryStore {
    override fun find(code: String): PrimaryDropEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryDropEntry) {
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

    override fun list(): MutableList<PrimaryDropEntry> {
        val out = mutableListOf<PrimaryDropEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryDropEntryStore(): PrimaryDropEntryStore {
    return MemoryPrimaryDropEntryStore(mutableMapOf<String, PrimaryDropEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryDropEntryStore(
    var inner: PrimaryDropEntryStore,
    var cache: TtlCache<PrimaryDropEntry>,
    var hits: Int,
    var misses: Int,
) : PrimaryDropEntryStore {
    override fun find(code: String): PrimaryDropEntry? {
        val hit = (cache.get(code) as PrimaryDropEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryDropEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryDropEntry> = inner.list()
}

fun newCachedPrimaryDropEntryStore(inner: PrimaryDropEntryStore, ttl: Int, clock: Clock): PrimaryDropEntryStore {
    return CachedPrimaryDropEntryStore(
        inner = inner,
        cache = newTtlCache<PrimaryDropEntry>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary drop entries. Empty fields match all. */
data class PrimaryDropEntryQuery(
    var title: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryDropEntry): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryDropEntry>): MutableList<PrimaryDropEntry> {
        val matched = filter(items) { candidate: PrimaryDropEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryDropEntries(store: PrimaryDropEntryStore, query: PrimaryDropEntryQuery): MutableList<PrimaryDropEntry> {
    return query.apply(store.list())
}
