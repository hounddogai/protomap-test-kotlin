package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling rest break entries by code. */
interface RollingRestBreakEntryStore {
    fun find(code: String): RollingRestBreakEntry?
    fun save(item: RollingRestBreakEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingRestBreakEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingRestBreakEntryStore(
    var items: MutableMap<String, RollingRestBreakEntry>,
    var order: MutableList<String>,
) : RollingRestBreakEntryStore {
    override fun find(code: String): RollingRestBreakEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingRestBreakEntry) {
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

    override fun list(): MutableList<RollingRestBreakEntry> {
        val out = mutableListOf<RollingRestBreakEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingRestBreakEntryStore(): RollingRestBreakEntryStore {
    return MemoryRollingRestBreakEntryStore(
        mutableMapOf<String, RollingRestBreakEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingRestBreakEntryStore(
    var inner: RollingRestBreakEntryStore,
    var cache: TtlCache<RollingRestBreakEntry>,
    var hits: Int,
    var misses: Int,
) : RollingRestBreakEntryStore {
    override fun find(code: String): RollingRestBreakEntry? {
        val hit = (cache.get(code) as RollingRestBreakEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingRestBreakEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingRestBreakEntry> = inner.list()
}

fun newCachedRollingRestBreakEntryStore(inner: RollingRestBreakEntryStore, ttl: Int, clock: Clock): RollingRestBreakEntryStore {
    return CachedRollingRestBreakEntryStore(
        inner = inner,
        cache = newTtlCache<RollingRestBreakEntry>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling rest break entries. Empty fields match all. */
data class RollingRestBreakEntryQuery(
    var depot: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingRestBreakEntry): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingRestBreakEntry>): MutableList<RollingRestBreakEntry> {
        val matched = filter(items) { candidate: RollingRestBreakEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingRestBreakEntries(store: RollingRestBreakEntryStore, query: RollingRestBreakEntryQuery): MutableList<RollingRestBreakEntry> {
    return query.apply(store.list())
}
