package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event records by code. */
interface CameraEventRecordStore {
    fun find(code: String): CameraEventRecord?
    fun save(item: CameraEventRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventRecordStore(
    var items: MutableMap<String, CameraEventRecord>,
    var order: MutableList<String>,
) : CameraEventRecordStore {
    override fun find(code: String): CameraEventRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventRecord) {
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

    override fun list(): MutableList<CameraEventRecord> {
        val out = mutableListOf<CameraEventRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventRecordStore(): CameraEventRecordStore {
    return MemoryCameraEventRecordStore(mutableMapOf<String, CameraEventRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventRecordStore(
    var inner: CameraEventRecordStore,
    var cache: TtlCache<CameraEventRecord>,
    var hits: Int,
    var misses: Int,
) : CameraEventRecordStore {
    override fun find(code: String): CameraEventRecord? {
        val hit = (cache.get(code) as CameraEventRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventRecord> = inner.list()
}

fun newCachedCameraEventRecordStore(inner: CameraEventRecordStore, ttl: Int, clock: Clock): CameraEventRecordStore {
    return CachedCameraEventRecordStore(
        inner = inner,
        cache = newTtlCache<CameraEventRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event records. Empty fields match all. */
data class CameraEventRecordQuery(
    var unit: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventRecord): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventRecord>): MutableList<CameraEventRecord> {
        val matched = filter(items) { candidate: CameraEventRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventRecords(store: CameraEventRecordStore, query: CameraEventRecordQuery): MutableList<CameraEventRecord> {
    return query.apply(store.list())
}
