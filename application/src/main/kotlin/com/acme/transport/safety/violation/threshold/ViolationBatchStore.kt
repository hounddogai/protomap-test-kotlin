package com.acme.transport.safety.violation.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation batches by code. */
interface ViolationBatchStore {
    fun find(code: String): ViolationBatch?
    fun save(item: ViolationBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationBatchStore(
    var items: MutableMap<String, ViolationBatch>,
    var order: MutableList<String>,
) : ViolationBatchStore {
    override fun find(code: String): ViolationBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationBatch) {
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

    override fun list(): MutableList<ViolationBatch> {
        val out = mutableListOf<ViolationBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationBatchStore(): ViolationBatchStore {
    return MemoryViolationBatchStore(mutableMapOf<String, ViolationBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationBatchStore(
    var inner: ViolationBatchStore,
    var cache: TtlCache<ViolationBatch>,
    var hits: Int,
    var misses: Int,
) : ViolationBatchStore {
    override fun find(code: String): ViolationBatch? {
        val hit = (cache.get(code) as ViolationBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationBatch> = inner.list()
}

fun newCachedViolationBatchStore(inner: ViolationBatchStore, ttl: Int, clock: Clock): ViolationBatchStore {
    return CachedViolationBatchStore(
        inner = inner,
        cache = newTtlCache<ViolationBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation batches. Empty fields match all. */
data class ViolationBatchQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationBatch): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationBatch>): MutableList<ViolationBatch> {
        val matched = filter(items) { candidate: ViolationBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationBatches(store: ViolationBatchStore, query: ViolationBatchQuery): MutableList<ViolationBatch> {
    return query.apply(store.list())
}
