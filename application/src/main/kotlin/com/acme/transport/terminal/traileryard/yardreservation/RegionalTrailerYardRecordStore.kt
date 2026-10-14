package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps regional trailer yard records by code. */
interface RegionalTrailerYardRecordStore {
    fun find(code: String): RegionalTrailerYardRecord?
    fun save(item: RegionalTrailerYardRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalTrailerYardRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalTrailerYardRecordStore(
    var items: MutableMap<String, RegionalTrailerYardRecord>,
    var order: MutableList<String>,
) : RegionalTrailerYardRecordStore {
    override fun find(code: String): RegionalTrailerYardRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalTrailerYardRecord) {
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

    override fun list(): MutableList<RegionalTrailerYardRecord> {
        val out = mutableListOf<RegionalTrailerYardRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalTrailerYardRecordStore(): RegionalTrailerYardRecordStore {
    return MemoryRegionalTrailerYardRecordStore(
        mutableMapOf<String, RegionalTrailerYardRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalTrailerYardRecordStore(
    var inner: RegionalTrailerYardRecordStore,
    var cache: TtlCache<RegionalTrailerYardRecord>,
    var hits: Int,
    var misses: Int,
) : RegionalTrailerYardRecordStore {
    override fun find(code: String): RegionalTrailerYardRecord? {
        val hit = (cache.get(code) as RegionalTrailerYardRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalTrailerYardRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalTrailerYardRecord> = inner.list()
}

fun newCachedRegionalTrailerYardRecordStore(inner: RegionalTrailerYardRecordStore, ttl: Int, clock: Clock): RegionalTrailerYardRecordStore {
    return CachedRegionalTrailerYardRecordStore(
        inner = inner,
        cache = newTtlCache<RegionalTrailerYardRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional trailer yard records. Empty fields match all. */
data class RegionalTrailerYardRecordQuery(
    var region: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalTrailerYardRecord): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalTrailerYardRecord>): MutableList<RegionalTrailerYardRecord> {
        val matched = filter(items) { candidate: RegionalTrailerYardRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalTrailerYardRecords(store: RegionalTrailerYardRecordStore, query: RegionalTrailerYardRecordQuery): MutableList<RegionalTrailerYardRecord> {
    return query.apply(store.list())
}
