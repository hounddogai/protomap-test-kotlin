package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps primary cutoff entries by code. */
interface PrimaryCutoffEntryStore {
    fun find(code: String): PrimaryCutoffEntry?
    fun save(item: PrimaryCutoffEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCutoffEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCutoffEntryStore(
    var items: MutableMap<String, PrimaryCutoffEntry>,
    var order: MutableList<String>,
) : PrimaryCutoffEntryStore {
    override fun find(code: String): PrimaryCutoffEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCutoffEntry) {
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

    override fun list(): MutableList<PrimaryCutoffEntry> {
        val out = mutableListOf<PrimaryCutoffEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCutoffEntryStore(): PrimaryCutoffEntryStore {
    return MemoryPrimaryCutoffEntryStore(mutableMapOf<String, PrimaryCutoffEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCutoffEntryStore(
    var inner: PrimaryCutoffEntryStore,
    var cache: TtlCache<PrimaryCutoffEntry>,
    var hits: Int,
    var misses: Int,
) : PrimaryCutoffEntryStore {
    override fun find(code: String): PrimaryCutoffEntry? {
        val hit = (cache.get(code) as PrimaryCutoffEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCutoffEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCutoffEntry> = inner.list()
}

fun newCachedPrimaryCutoffEntryStore(inner: PrimaryCutoffEntryStore, ttl: Int, clock: Clock): PrimaryCutoffEntryStore {
    return CachedPrimaryCutoffEntryStore(
        inner = inner,
        cache = newTtlCache<PrimaryCutoffEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary cutoff entries. Empty fields match all. */
data class PrimaryCutoffEntryQuery(
    var channel: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCutoffEntry): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<PrimaryCutoffEntry>): MutableList<PrimaryCutoffEntry> {
        val matched = filter(items) { candidate: PrimaryCutoffEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCutoffEntries(store: PrimaryCutoffEntryStore, query: PrimaryCutoffEntryQuery): MutableList<PrimaryCutoffEntry> {
    return query.apply(store.list())
}
