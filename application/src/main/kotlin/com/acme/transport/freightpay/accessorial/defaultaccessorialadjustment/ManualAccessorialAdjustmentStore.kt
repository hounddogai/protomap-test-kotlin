package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual accessorial adjustments by code. */
interface ManualAccessorialAdjustmentStore {
    fun find(code: String): ManualAccessorialAdjustment?
    fun save(item: ManualAccessorialAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualAccessorialAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualAccessorialAdjustmentStore(
    var items: MutableMap<String, ManualAccessorialAdjustment>,
    var order: MutableList<String>,
) : ManualAccessorialAdjustmentStore {
    override fun find(code: String): ManualAccessorialAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualAccessorialAdjustment) {
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

    override fun list(): MutableList<ManualAccessorialAdjustment> {
        val out = mutableListOf<ManualAccessorialAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualAccessorialAdjustmentStore(): ManualAccessorialAdjustmentStore {
    return MemoryManualAccessorialAdjustmentStore(
        mutableMapOf<String, ManualAccessorialAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualAccessorialAdjustmentStore(
    var inner: ManualAccessorialAdjustmentStore,
    var cache: TtlCache<ManualAccessorialAdjustment>,
    var hits: Int,
    var misses: Int,
) : ManualAccessorialAdjustmentStore {
    override fun find(code: String): ManualAccessorialAdjustment? {
        val hit = (cache.get(code) as ManualAccessorialAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualAccessorialAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualAccessorialAdjustment> = inner.list()
}

fun newCachedManualAccessorialAdjustmentStore(inner: ManualAccessorialAdjustmentStore, ttl: Int, clock: Clock): ManualAccessorialAdjustmentStore {
    return CachedManualAccessorialAdjustmentStore(
        inner = inner,
        cache = newTtlCache<ManualAccessorialAdjustment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual accessorial adjustments. Empty fields match all. */
data class ManualAccessorialAdjustmentQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualAccessorialAdjustment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualAccessorialAdjustment>): MutableList<ManualAccessorialAdjustment> {
        val matched = filter(items) { candidate: ManualAccessorialAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualAccessorialAdjustments(store: ManualAccessorialAdjustmentStore, query: ManualAccessorialAdjustmentQuery): MutableList<ManualAccessorialAdjustment> {
    return query.apply(store.list())
}
