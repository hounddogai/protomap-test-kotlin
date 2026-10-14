package com.acme.transport.fleetops.leasereturn.recorditem

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional lease return records by code. */
interface RegionalLeaseReturnRecordStore {
    fun find(code: String): RegionalLeaseReturnRecord?
    fun save(item: RegionalLeaseReturnRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalLeaseReturnRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalLeaseReturnRecordStore(
    var items: MutableMap<String, RegionalLeaseReturnRecord>,
    var order: MutableList<String>,
) : RegionalLeaseReturnRecordStore {
    override fun find(code: String): RegionalLeaseReturnRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalLeaseReturnRecord) {
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

    override fun list(): MutableList<RegionalLeaseReturnRecord> {
        val out = mutableListOf<RegionalLeaseReturnRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalLeaseReturnRecordStore(): RegionalLeaseReturnRecordStore {
    return MemoryRegionalLeaseReturnRecordStore(
        mutableMapOf<String, RegionalLeaseReturnRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalLeaseReturnRecordStore(
    var inner: RegionalLeaseReturnRecordStore,
    var cache: TtlCache<RegionalLeaseReturnRecord>,
    var hits: Int,
    var misses: Int,
) : RegionalLeaseReturnRecordStore {
    override fun find(code: String): RegionalLeaseReturnRecord? {
        val hit = (cache.get(code) as RegionalLeaseReturnRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalLeaseReturnRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalLeaseReturnRecord> = inner.list()
}

fun newCachedRegionalLeaseReturnRecordStore(inner: RegionalLeaseReturnRecordStore, ttl: Int, clock: Clock): RegionalLeaseReturnRecordStore {
    return CachedRegionalLeaseReturnRecordStore(
        inner = inner,
        cache = newTtlCache<RegionalLeaseReturnRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional lease return records. Empty fields match all. */
data class RegionalLeaseReturnRecordQuery(
    var source: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalLeaseReturnRecord): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalLeaseReturnRecord>): MutableList<RegionalLeaseReturnRecord> {
        val matched = filter(items) { candidate: RegionalLeaseReturnRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalLeaseReturnRecords(store: RegionalLeaseReturnRecordStore, query: RegionalLeaseReturnRecordQuery): MutableList<RegionalLeaseReturnRecord> {
    return query.apply(store.list())
}
