package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled cross dock records by code. */
interface ScheduledCrossDockRecordStore {
    fun find(code: String): ScheduledCrossDockRecord?
    fun save(item: ScheduledCrossDockRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledCrossDockRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledCrossDockRecordStore(
    var items: MutableMap<String, ScheduledCrossDockRecord>,
    var order: MutableList<String>,
) : ScheduledCrossDockRecordStore {
    override fun find(code: String): ScheduledCrossDockRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledCrossDockRecord) {
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

    override fun list(): MutableList<ScheduledCrossDockRecord> {
        val out = mutableListOf<ScheduledCrossDockRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledCrossDockRecordStore(): ScheduledCrossDockRecordStore {
    return MemoryScheduledCrossDockRecordStore(
        mutableMapOf<String, ScheduledCrossDockRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledCrossDockRecordStore(
    var inner: ScheduledCrossDockRecordStore,
    var cache: TtlCache<ScheduledCrossDockRecord>,
    var hits: Int,
    var misses: Int,
) : ScheduledCrossDockRecordStore {
    override fun find(code: String): ScheduledCrossDockRecord? {
        val hit = (cache.get(code) as ScheduledCrossDockRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledCrossDockRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledCrossDockRecord> = inner.list()
}

fun newCachedScheduledCrossDockRecordStore(inner: ScheduledCrossDockRecordStore, ttl: Int, clock: Clock): ScheduledCrossDockRecordStore {
    return CachedScheduledCrossDockRecordStore(
        inner = inner,
        cache = newTtlCache<ScheduledCrossDockRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled cross dock records. Empty fields match all. */
data class ScheduledCrossDockRecordQuery(
    var segment: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledCrossDockRecord): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledCrossDockRecord>): MutableList<ScheduledCrossDockRecord> {
        val matched = filter(items) { candidate: ScheduledCrossDockRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledCrossDockRecords(store: ScheduledCrossDockRecordStore, query: ScheduledCrossDockRecordQuery): MutableList<ScheduledCrossDockRecord> {
    return query.apply(store.list())
}
