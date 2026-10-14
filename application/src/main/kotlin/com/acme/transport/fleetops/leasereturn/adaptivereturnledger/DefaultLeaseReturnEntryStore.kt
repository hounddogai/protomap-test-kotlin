package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default lease return entries by code. */
interface DefaultLeaseReturnEntryStore {
    fun find(code: String): DefaultLeaseReturnEntry?
    fun save(item: DefaultLeaseReturnEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultLeaseReturnEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultLeaseReturnEntryStore(
    var items: MutableMap<String, DefaultLeaseReturnEntry>,
    var order: MutableList<String>,
) : DefaultLeaseReturnEntryStore {
    override fun find(code: String): DefaultLeaseReturnEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultLeaseReturnEntry) {
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

    override fun list(): MutableList<DefaultLeaseReturnEntry> {
        val out = mutableListOf<DefaultLeaseReturnEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultLeaseReturnEntryStore(): DefaultLeaseReturnEntryStore {
    return MemoryDefaultLeaseReturnEntryStore(
        mutableMapOf<String, DefaultLeaseReturnEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultLeaseReturnEntryStore(
    var inner: DefaultLeaseReturnEntryStore,
    var cache: TtlCache<DefaultLeaseReturnEntry>,
    var hits: Int,
    var misses: Int,
) : DefaultLeaseReturnEntryStore {
    override fun find(code: String): DefaultLeaseReturnEntry? {
        val hit = (cache.get(code) as DefaultLeaseReturnEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultLeaseReturnEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultLeaseReturnEntry> = inner.list()
}

fun newCachedDefaultLeaseReturnEntryStore(inner: DefaultLeaseReturnEntryStore, ttl: Int, clock: Clock): DefaultLeaseReturnEntryStore {
    return CachedDefaultLeaseReturnEntryStore(
        inner = inner,
        cache = newTtlCache<DefaultLeaseReturnEntry>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default lease return entries. Empty fields match all. */
data class DefaultLeaseReturnEntryQuery(
    var name: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultLeaseReturnEntry): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultLeaseReturnEntry>): MutableList<DefaultLeaseReturnEntry> {
        val matched = filter(items) { candidate: DefaultLeaseReturnEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultLeaseReturnEntries(store: DefaultLeaseReturnEntryStore, query: DefaultLeaseReturnEntryQuery): MutableList<DefaultLeaseReturnEntry> {
    return query.apply(store.list())
}
