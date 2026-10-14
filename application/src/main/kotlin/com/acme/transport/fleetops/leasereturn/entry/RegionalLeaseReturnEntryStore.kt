package com.acme.transport.fleetops.leasereturn.entry

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional lease return entries by code. */
interface RegionalLeaseReturnEntryStore {
    fun find(code: String): RegionalLeaseReturnEntry?
    fun save(item: RegionalLeaseReturnEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalLeaseReturnEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalLeaseReturnEntryStore(
    var items: MutableMap<String, RegionalLeaseReturnEntry>,
    var order: MutableList<String>,
) : RegionalLeaseReturnEntryStore {
    override fun find(code: String): RegionalLeaseReturnEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalLeaseReturnEntry) {
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

    override fun list(): MutableList<RegionalLeaseReturnEntry> {
        val out = mutableListOf<RegionalLeaseReturnEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalLeaseReturnEntryStore(): RegionalLeaseReturnEntryStore {
    return MemoryRegionalLeaseReturnEntryStore(
        mutableMapOf<String, RegionalLeaseReturnEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalLeaseReturnEntryStore(
    var inner: RegionalLeaseReturnEntryStore,
    var cache: TtlCache<RegionalLeaseReturnEntry>,
    var hits: Int,
    var misses: Int,
) : RegionalLeaseReturnEntryStore {
    override fun find(code: String): RegionalLeaseReturnEntry? {
        val hit = (cache.get(code) as RegionalLeaseReturnEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalLeaseReturnEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalLeaseReturnEntry> = inner.list()
}

fun newCachedRegionalLeaseReturnEntryStore(inner: RegionalLeaseReturnEntryStore, ttl: Int, clock: Clock): RegionalLeaseReturnEntryStore {
    return CachedRegionalLeaseReturnEntryStore(
        inner = inner,
        cache = newTtlCache<RegionalLeaseReturnEntry>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional lease return entries. Empty fields match all. */
data class RegionalLeaseReturnEntryQuery(
    var name: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalLeaseReturnEntry): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalLeaseReturnEntry>): MutableList<RegionalLeaseReturnEntry> {
        val matched = filter(items) { candidate: RegionalLeaseReturnEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalLeaseReturnEntries(store: RegionalLeaseReturnEntryStore, query: RegionalLeaseReturnEntryQuery): MutableList<RegionalLeaseReturnEntry> {
    return query.apply(store.list())
}
