package com.acme.transport.freightpay.accessorial.cycle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial batches by code. */
interface AccessorialBatchStore {
    fun find(code: String): AccessorialBatch?
    fun save(item: AccessorialBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialBatchStore(
    var items: MutableMap<String, AccessorialBatch>,
    var order: MutableList<String>,
) : AccessorialBatchStore {
    override fun find(code: String): AccessorialBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialBatch) {
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

    override fun list(): MutableList<AccessorialBatch> {
        val out = mutableListOf<AccessorialBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialBatchStore(): AccessorialBatchStore {
    return MemoryAccessorialBatchStore(mutableMapOf<String, AccessorialBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialBatchStore(
    var inner: AccessorialBatchStore,
    var cache: TtlCache<AccessorialBatch>,
    var hits: Int,
    var misses: Int,
) : AccessorialBatchStore {
    override fun find(code: String): AccessorialBatch? {
        val hit = (cache.get(code) as AccessorialBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialBatch> = inner.list()
}

fun newCachedAccessorialBatchStore(inner: AccessorialBatchStore, ttl: Int, clock: Clock): AccessorialBatchStore {
    return CachedAccessorialBatchStore(
        inner = inner,
        cache = newTtlCache<AccessorialBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial batches. Empty fields match all. */
data class AccessorialBatchQuery(
    var label: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialBatch): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<AccessorialBatch>): MutableList<AccessorialBatch> {
        val matched = filter(items) { candidate: AccessorialBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialBatches(store: AccessorialBatchStore, query: AccessorialBatchQuery): MutableList<AccessorialBatch> {
    return query.apply(store.list())
}
