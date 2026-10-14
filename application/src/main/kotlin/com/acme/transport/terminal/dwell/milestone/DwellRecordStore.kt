package com.acme.transport.terminal.dwell.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell records by code. */
interface DwellRecordStore {
    fun find(code: String): DwellRecord?
    fun save(item: DwellRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellRecordStore(
    var items: MutableMap<String, DwellRecord>,
    var order: MutableList<String>,
) : DwellRecordStore {
    override fun find(code: String): DwellRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellRecord) {
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

    override fun list(): MutableList<DwellRecord> {
        val out = mutableListOf<DwellRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellRecordStore(): DwellRecordStore {
    return MemoryDwellRecordStore(mutableMapOf<String, DwellRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellRecordStore(
    var inner: DwellRecordStore,
    var cache: TtlCache<DwellRecord>,
    var hits: Int,
    var misses: Int,
) : DwellRecordStore {
    override fun find(code: String): DwellRecord? {
        val hit = (cache.get(code) as DwellRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellRecord> = inner.list()
}

fun newCachedDwellRecordStore(inner: DwellRecordStore, ttl: Int, clock: Clock): DwellRecordStore {
    return CachedDwellRecordStore(
        inner = inner,
        cache = newTtlCache<DwellRecord>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell records. Empty fields match all. */
data class DwellRecordQuery(
    var region: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellRecord): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellRecord>): MutableList<DwellRecord> {
        val matched = filter(items) { candidate: DwellRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellRecords(store: DwellRecordStore, query: DwellRecordQuery): MutableList<DwellRecord> {
    return query.apply(store.list())
}
