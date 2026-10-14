package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled dock door records by code. */
interface ScheduledDockDoorRecordStore {
    fun find(code: String): ScheduledDockDoorRecord?
    fun save(item: ScheduledDockDoorRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDockDoorRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDockDoorRecordStore(
    var items: MutableMap<String, ScheduledDockDoorRecord>,
    var order: MutableList<String>,
) : ScheduledDockDoorRecordStore {
    override fun find(code: String): ScheduledDockDoorRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDockDoorRecord) {
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

    override fun list(): MutableList<ScheduledDockDoorRecord> {
        val out = mutableListOf<ScheduledDockDoorRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDockDoorRecordStore(): ScheduledDockDoorRecordStore {
    return MemoryScheduledDockDoorRecordStore(
        mutableMapOf<String, ScheduledDockDoorRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDockDoorRecordStore(
    var inner: ScheduledDockDoorRecordStore,
    var cache: TtlCache<ScheduledDockDoorRecord>,
    var hits: Int,
    var misses: Int,
) : ScheduledDockDoorRecordStore {
    override fun find(code: String): ScheduledDockDoorRecord? {
        val hit = (cache.get(code) as ScheduledDockDoorRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDockDoorRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDockDoorRecord> = inner.list()
}

fun newCachedScheduledDockDoorRecordStore(inner: ScheduledDockDoorRecordStore, ttl: Int, clock: Clock): ScheduledDockDoorRecordStore {
    return CachedScheduledDockDoorRecordStore(
        inner = inner,
        cache = newTtlCache<ScheduledDockDoorRecord>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled dock door records. Empty fields match all. */
data class ScheduledDockDoorRecordQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDockDoorRecord): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledDockDoorRecord>): MutableList<ScheduledDockDoorRecord> {
        val matched = filter(items) { candidate: ScheduledDockDoorRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDockDoorRecords(store: ScheduledDockDoorRecordStore, query: ScheduledDockDoorRecordQuery): MutableList<ScheduledDockDoorRecord> {
    return query.apply(store.list())
}
