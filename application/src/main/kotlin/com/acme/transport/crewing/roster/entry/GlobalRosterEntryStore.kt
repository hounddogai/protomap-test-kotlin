package com.acme.transport.crewing.roster.entry

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global roster entries by code. */
interface GlobalRosterEntryStore {
    fun find(code: String): GlobalRosterEntry?
    fun save(item: GlobalRosterEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalRosterEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalRosterEntryStore(
    var items: MutableMap<String, GlobalRosterEntry>,
    var order: MutableList<String>,
) : GlobalRosterEntryStore {
    override fun find(code: String): GlobalRosterEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalRosterEntry) {
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

    override fun list(): MutableList<GlobalRosterEntry> {
        val out = mutableListOf<GlobalRosterEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalRosterEntryStore(): GlobalRosterEntryStore {
    return MemoryGlobalRosterEntryStore(mutableMapOf<String, GlobalRosterEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalRosterEntryStore(
    var inner: GlobalRosterEntryStore,
    var cache: TtlCache<GlobalRosterEntry>,
    var hits: Int,
    var misses: Int,
) : GlobalRosterEntryStore {
    override fun find(code: String): GlobalRosterEntry? {
        val hit = (cache.get(code) as GlobalRosterEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalRosterEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalRosterEntry> = inner.list()
}

fun newCachedGlobalRosterEntryStore(inner: GlobalRosterEntryStore, ttl: Int, clock: Clock): GlobalRosterEntryStore {
    return CachedGlobalRosterEntryStore(
        inner = inner,
        cache = newTtlCache<GlobalRosterEntry>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global roster entries. Empty fields match all. */
data class GlobalRosterEntryQuery(
    var name: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalRosterEntry): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalRosterEntry>): MutableList<GlobalRosterEntry> {
        val matched = filter(items) { candidate: GlobalRosterEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalRosterEntries(store: GlobalRosterEntryStore, query: GlobalRosterEntryQuery): MutableList<GlobalRosterEntry> {
    return query.apply(store.list())
}
