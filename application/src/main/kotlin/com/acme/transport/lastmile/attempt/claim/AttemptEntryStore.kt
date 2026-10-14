package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt entries by code. */
interface AttemptEntryStore {
    fun find(code: String): AttemptEntry?
    fun save(item: AttemptEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptEntryStore(
    var items: MutableMap<String, AttemptEntry>,
    var order: MutableList<String>,
) : AttemptEntryStore {
    override fun find(code: String): AttemptEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptEntry) {
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

    override fun list(): MutableList<AttemptEntry> {
        val out = mutableListOf<AttemptEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptEntryStore(): AttemptEntryStore {
    return MemoryAttemptEntryStore(mutableMapOf<String, AttemptEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptEntryStore(
    var inner: AttemptEntryStore,
    var cache: TtlCache<AttemptEntry>,
    var hits: Int,
    var misses: Int,
) : AttemptEntryStore {
    override fun find(code: String): AttemptEntry? {
        val hit = (cache.get(code) as AttemptEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptEntry> = inner.list()
}

fun newCachedAttemptEntryStore(inner: AttemptEntryStore, ttl: Int, clock: Clock): AttemptEntryStore {
    return CachedAttemptEntryStore(
        inner = inner,
        cache = newTtlCache<AttemptEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt entries. Empty fields match all. */
data class AttemptEntryQuery(
    var unit: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptEntry): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptEntry>): MutableList<AttemptEntry> {
        val matched = filter(items) { candidate: AttemptEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptEntries(store: AttemptEntryStore, query: AttemptEntryQuery): MutableList<AttemptEntry> {
    return query.apply(store.list())
}
