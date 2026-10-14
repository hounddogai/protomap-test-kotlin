package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling roster entries by code. */
interface RollingRosterEntryStore {
    fun find(code: String): RollingRosterEntry?
    fun save(item: RollingRosterEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingRosterEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingRosterEntryStore(
    var items: MutableMap<String, RollingRosterEntry>,
    var order: MutableList<String>,
) : RollingRosterEntryStore {
    override fun find(code: String): RollingRosterEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingRosterEntry) {
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

    override fun list(): MutableList<RollingRosterEntry> {
        val out = mutableListOf<RollingRosterEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingRosterEntryStore(): RollingRosterEntryStore {
    return MemoryRollingRosterEntryStore(mutableMapOf<String, RollingRosterEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingRosterEntryStore(
    var inner: RollingRosterEntryStore,
    var cache: TtlCache<RollingRosterEntry>,
    var hits: Int,
    var misses: Int,
) : RollingRosterEntryStore {
    override fun find(code: String): RollingRosterEntry? {
        val hit = (cache.get(code) as RollingRosterEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingRosterEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingRosterEntry> = inner.list()
}

fun newCachedRollingRosterEntryStore(inner: RollingRosterEntryStore, ttl: Int, clock: Clock): RollingRosterEntryStore {
    return CachedRollingRosterEntryStore(
        inner = inner,
        cache = newTtlCache<RollingRosterEntry>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling roster entries. Empty fields match all. */
data class RollingRosterEntryQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingRosterEntry): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingRosterEntry>): MutableList<RollingRosterEntry> {
        val matched = filter(items) { candidate: RollingRosterEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingRosterEntries(store: RollingRosterEntryStore, query: RollingRosterEntryQuery): MutableList<RollingRosterEntry> {
    return query.apply(store.list())
}
