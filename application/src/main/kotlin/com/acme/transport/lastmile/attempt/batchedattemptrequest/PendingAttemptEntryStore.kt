package com.acme.transport.lastmile.attempt.batchedattemptrequest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps pending attempt entries by code. */
interface PendingAttemptEntryStore {
    fun find(code: String): PendingAttemptEntry?
    fun save(item: PendingAttemptEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingAttemptEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingAttemptEntryStore(
    var items: MutableMap<String, PendingAttemptEntry>,
    var order: MutableList<String>,
) : PendingAttemptEntryStore {
    override fun find(code: String): PendingAttemptEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingAttemptEntry) {
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

    override fun list(): MutableList<PendingAttemptEntry> {
        val out = mutableListOf<PendingAttemptEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingAttemptEntryStore(): PendingAttemptEntryStore {
    return MemoryPendingAttemptEntryStore(mutableMapOf<String, PendingAttemptEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingAttemptEntryStore(
    var inner: PendingAttemptEntryStore,
    var cache: TtlCache<PendingAttemptEntry>,
    var hits: Int,
    var misses: Int,
) : PendingAttemptEntryStore {
    override fun find(code: String): PendingAttemptEntry? {
        val hit = (cache.get(code) as PendingAttemptEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingAttemptEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingAttemptEntry> = inner.list()
}

fun newCachedPendingAttemptEntryStore(inner: PendingAttemptEntryStore, ttl: Int, clock: Clock): PendingAttemptEntryStore {
    return CachedPendingAttemptEntryStore(
        inner = inner,
        cache = newTtlCache<PendingAttemptEntry>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending attempt entries. Empty fields match all. */
data class PendingAttemptEntryQuery(
    var name: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingAttemptEntry): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingAttemptEntry>): MutableList<PendingAttemptEntry> {
        val matched = filter(items) { candidate: PendingAttemptEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingAttemptEntries(store: PendingAttemptEntryStore, query: PendingAttemptEntryQuery): MutableList<PendingAttemptEntry> {
    return query.apply(store.list())
}
