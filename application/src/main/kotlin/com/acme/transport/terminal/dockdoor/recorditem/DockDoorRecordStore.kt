package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door records by code. */
interface DockDoorRecordStore {
    fun find(code: String): DockDoorRecord?
    fun save(item: DockDoorRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorRecordStore(
    var items: MutableMap<String, DockDoorRecord>,
    var order: MutableList<String>,
) : DockDoorRecordStore {
    override fun find(code: String): DockDoorRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorRecord) {
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

    override fun list(): MutableList<DockDoorRecord> {
        val out = mutableListOf<DockDoorRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorRecordStore(): DockDoorRecordStore {
    return MemoryDockDoorRecordStore(mutableMapOf<String, DockDoorRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorRecordStore(
    var inner: DockDoorRecordStore,
    var cache: TtlCache<DockDoorRecord>,
    var hits: Int,
    var misses: Int,
) : DockDoorRecordStore {
    override fun find(code: String): DockDoorRecord? {
        val hit = (cache.get(code) as DockDoorRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorRecord> = inner.list()
}

fun newCachedDockDoorRecordStore(inner: DockDoorRecordStore, ttl: Int, clock: Clock): DockDoorRecordStore {
    return CachedDockDoorRecordStore(
        inner = inner,
        cache = newTtlCache<DockDoorRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door records. Empty fields match all. */
data class DockDoorRecordQuery(
    var country: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorRecord): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<DockDoorRecord>): MutableList<DockDoorRecord> {
        val matched = filter(items) { candidate: DockDoorRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorRecords(store: DockDoorRecordStore, query: DockDoorRecordQuery): MutableList<DockDoorRecord> {
    return query.apply(store.list())
}
