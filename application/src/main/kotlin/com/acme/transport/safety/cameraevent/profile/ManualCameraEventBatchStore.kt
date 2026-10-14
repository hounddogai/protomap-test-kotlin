package com.acme.transport.safety.cameraevent.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps manual camera event batches by code. */
interface ManualCameraEventBatchStore {
    fun find(code: String): ManualCameraEventBatch?
    fun save(item: ManualCameraEventBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualCameraEventBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualCameraEventBatchStore(
    var items: MutableMap<String, ManualCameraEventBatch>,
    var order: MutableList<String>,
) : ManualCameraEventBatchStore {
    override fun find(code: String): ManualCameraEventBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualCameraEventBatch) {
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

    override fun list(): MutableList<ManualCameraEventBatch> {
        val out = mutableListOf<ManualCameraEventBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualCameraEventBatchStore(): ManualCameraEventBatchStore {
    return MemoryManualCameraEventBatchStore(
        mutableMapOf<String, ManualCameraEventBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualCameraEventBatchStore(
    var inner: ManualCameraEventBatchStore,
    var cache: TtlCache<ManualCameraEventBatch>,
    var hits: Int,
    var misses: Int,
) : ManualCameraEventBatchStore {
    override fun find(code: String): ManualCameraEventBatch? {
        val hit = (cache.get(code) as ManualCameraEventBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualCameraEventBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualCameraEventBatch> = inner.list()
}

fun newCachedManualCameraEventBatchStore(inner: ManualCameraEventBatchStore, ttl: Int, clock: Clock): ManualCameraEventBatchStore {
    return CachedManualCameraEventBatchStore(
        inner = inner,
        cache = newTtlCache<ManualCameraEventBatch>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual camera event batches. Empty fields match all. */
data class ManualCameraEventBatchQuery(
    var title: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualCameraEventBatch): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualCameraEventBatch>): MutableList<ManualCameraEventBatch> {
        val matched = filter(items) { candidate: ManualCameraEventBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualCameraEventBatches(store: ManualCameraEventBatchStore, query: ManualCameraEventBatchQuery): MutableList<ManualCameraEventBatch> {
    return query.apply(store.list())
}
