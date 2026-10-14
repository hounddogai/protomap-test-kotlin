package com.acme.transport.fleetops.leasereturn.mapping

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual lease return entries by code. */
interface ManualLeaseReturnEntryStore {
    fun find(code: String): ManualLeaseReturnEntry?
    fun save(item: ManualLeaseReturnEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualLeaseReturnEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualLeaseReturnEntryStore(
    var items: MutableMap<String, ManualLeaseReturnEntry>,
    var order: MutableList<String>,
) : ManualLeaseReturnEntryStore {
    override fun find(code: String): ManualLeaseReturnEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualLeaseReturnEntry) {
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

    override fun list(): MutableList<ManualLeaseReturnEntry> {
        val out = mutableListOf<ManualLeaseReturnEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualLeaseReturnEntryStore(): ManualLeaseReturnEntryStore {
    return MemoryManualLeaseReturnEntryStore(
        mutableMapOf<String, ManualLeaseReturnEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualLeaseReturnEntryStore(
    var inner: ManualLeaseReturnEntryStore,
    var cache: TtlCache<ManualLeaseReturnEntry>,
    var hits: Int,
    var misses: Int,
) : ManualLeaseReturnEntryStore {
    override fun find(code: String): ManualLeaseReturnEntry? {
        val hit = (cache.get(code) as ManualLeaseReturnEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualLeaseReturnEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualLeaseReturnEntry> = inner.list()
}

fun newCachedManualLeaseReturnEntryStore(inner: ManualLeaseReturnEntryStore, ttl: Int, clock: Clock): ManualLeaseReturnEntryStore {
    return CachedManualLeaseReturnEntryStore(
        inner = inner,
        cache = newTtlCache<ManualLeaseReturnEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual lease return entries. Empty fields match all. */
data class ManualLeaseReturnEntryQuery(
    var label: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualLeaseReturnEntry): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualLeaseReturnEntry>): MutableList<ManualLeaseReturnEntry> {
        val matched = filter(items) { candidate: ManualLeaseReturnEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualLeaseReturnEntries(store: ManualLeaseReturnEntryStore, query: ManualLeaseReturnEntryQuery): MutableList<ManualLeaseReturnEntry> {
    return query.apply(store.list())
}
