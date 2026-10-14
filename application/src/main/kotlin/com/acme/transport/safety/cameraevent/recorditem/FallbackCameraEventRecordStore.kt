package com.acme.transport.safety.cameraevent.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps fallback camera event records by code. */
interface FallbackCameraEventRecordStore {
    fun find(code: String): FallbackCameraEventRecord?
    fun save(item: FallbackCameraEventRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackCameraEventRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackCameraEventRecordStore(
    var items: MutableMap<String, FallbackCameraEventRecord>,
    var order: MutableList<String>,
) : FallbackCameraEventRecordStore {
    override fun find(code: String): FallbackCameraEventRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackCameraEventRecord) {
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

    override fun list(): MutableList<FallbackCameraEventRecord> {
        val out = mutableListOf<FallbackCameraEventRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackCameraEventRecordStore(): FallbackCameraEventRecordStore {
    return MemoryFallbackCameraEventRecordStore(
        mutableMapOf<String, FallbackCameraEventRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackCameraEventRecordStore(
    var inner: FallbackCameraEventRecordStore,
    var cache: TtlCache<FallbackCameraEventRecord>,
    var hits: Int,
    var misses: Int,
) : FallbackCameraEventRecordStore {
    override fun find(code: String): FallbackCameraEventRecord? {
        val hit = (cache.get(code) as FallbackCameraEventRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackCameraEventRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackCameraEventRecord> = inner.list()
}

fun newCachedFallbackCameraEventRecordStore(inner: FallbackCameraEventRecordStore, ttl: Int, clock: Clock): FallbackCameraEventRecordStore {
    return CachedFallbackCameraEventRecordStore(
        inner = inner,
        cache = newTtlCache<FallbackCameraEventRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback camera event records. Empty fields match all. */
data class FallbackCameraEventRecordQuery(
    var source: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackCameraEventRecord): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackCameraEventRecord>): MutableList<FallbackCameraEventRecord> {
        val matched = filter(items) { candidate: FallbackCameraEventRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackCameraEventRecords(store: FallbackCameraEventRecordStore, query: FallbackCameraEventRecordQuery): MutableList<FallbackCameraEventRecord> {
    return query.apply(store.list())
}
