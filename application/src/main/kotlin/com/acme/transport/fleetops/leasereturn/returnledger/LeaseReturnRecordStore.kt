package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return records by code. */
interface LeaseReturnRecordStore {
    fun find(code: String): LeaseReturnRecord?
    fun save(item: LeaseReturnRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnRecordStore(
    var items: MutableMap<String, LeaseReturnRecord>,
    var order: MutableList<String>,
) : LeaseReturnRecordStore {
    override fun find(code: String): LeaseReturnRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnRecord) {
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

    override fun list(): MutableList<LeaseReturnRecord> {
        val out = mutableListOf<LeaseReturnRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnRecordStore(): LeaseReturnRecordStore {
    return MemoryLeaseReturnRecordStore(mutableMapOf<String, LeaseReturnRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnRecordStore(
    var inner: LeaseReturnRecordStore,
    var cache: TtlCache<LeaseReturnRecord>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnRecordStore {
    override fun find(code: String): LeaseReturnRecord? {
        val hit = (cache.get(code) as LeaseReturnRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnRecord> = inner.list()
}

fun newCachedLeaseReturnRecordStore(inner: LeaseReturnRecordStore, ttl: Int, clock: Clock): LeaseReturnRecordStore {
    return CachedLeaseReturnRecordStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnRecord>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return records. Empty fields match all. */
data class LeaseReturnRecordQuery(
    var country: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnRecord): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnRecord>): MutableList<LeaseReturnRecord> {
        val matched = filter(items) { candidate: LeaseReturnRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnRecords(store: LeaseReturnRecordStore, query: LeaseReturnRecordQuery): MutableList<LeaseReturnRecord> {
    return query.apply(store.list())
}
