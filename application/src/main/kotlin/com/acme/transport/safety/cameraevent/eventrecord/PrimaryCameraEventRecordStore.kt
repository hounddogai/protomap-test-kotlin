package com.acme.transport.safety.cameraevent.eventrecord

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary camera event records by code. */
interface PrimaryCameraEventRecordStore {
    fun find(code: String): PrimaryCameraEventRecord?
    fun save(item: PrimaryCameraEventRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCameraEventRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCameraEventRecordStore(
    var items: MutableMap<String, PrimaryCameraEventRecord>,
    var order: MutableList<String>,
) : PrimaryCameraEventRecordStore {
    override fun find(code: String): PrimaryCameraEventRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCameraEventRecord) {
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

    override fun list(): MutableList<PrimaryCameraEventRecord> {
        val out = mutableListOf<PrimaryCameraEventRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCameraEventRecordStore(): PrimaryCameraEventRecordStore {
    return MemoryPrimaryCameraEventRecordStore(
        mutableMapOf<String, PrimaryCameraEventRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCameraEventRecordStore(
    var inner: PrimaryCameraEventRecordStore,
    var cache: TtlCache<PrimaryCameraEventRecord>,
    var hits: Int,
    var misses: Int,
) : PrimaryCameraEventRecordStore {
    override fun find(code: String): PrimaryCameraEventRecord? {
        val hit = (cache.get(code) as PrimaryCameraEventRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCameraEventRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCameraEventRecord> = inner.list()
}

fun newCachedPrimaryCameraEventRecordStore(inner: PrimaryCameraEventRecordStore, ttl: Int, clock: Clock): PrimaryCameraEventRecordStore {
    return CachedPrimaryCameraEventRecordStore(
        inner = inner,
        cache = newTtlCache<PrimaryCameraEventRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary camera event records. Empty fields match all. */
data class PrimaryCameraEventRecordQuery(
    var channel: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCameraEventRecord): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryCameraEventRecord>): MutableList<PrimaryCameraEventRecord> {
        val matched = filter(items) { candidate: PrimaryCameraEventRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCameraEventRecords(store: PrimaryCameraEventRecordStore, query: PrimaryCameraEventRecordQuery): MutableList<PrimaryCameraEventRecord> {
    return query.apply(store.list())
}
