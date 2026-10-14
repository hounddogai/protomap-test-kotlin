package com.acme.transport.freightpay.accessorial.adjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial adjustments by code. */
interface AccessorialAdjustmentStore {
    fun find(code: String): AccessorialAdjustment?
    fun save(item: AccessorialAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialAdjustmentStore(
    var items: MutableMap<String, AccessorialAdjustment>,
    var order: MutableList<String>,
) : AccessorialAdjustmentStore {
    override fun find(code: String): AccessorialAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialAdjustment) {
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

    override fun list(): MutableList<AccessorialAdjustment> {
        val out = mutableListOf<AccessorialAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialAdjustmentStore(): AccessorialAdjustmentStore {
    return MemoryAccessorialAdjustmentStore(
        mutableMapOf<String, AccessorialAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialAdjustmentStore(
    var inner: AccessorialAdjustmentStore,
    var cache: TtlCache<AccessorialAdjustment>,
    var hits: Int,
    var misses: Int,
) : AccessorialAdjustmentStore {
    override fun find(code: String): AccessorialAdjustment? {
        val hit = (cache.get(code) as AccessorialAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialAdjustment> = inner.list()
}

fun newCachedAccessorialAdjustmentStore(inner: AccessorialAdjustmentStore, ttl: Int, clock: Clock): AccessorialAdjustmentStore {
    return CachedAccessorialAdjustmentStore(
        inner = inner,
        cache = newTtlCache<AccessorialAdjustment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial adjustments. Empty fields match all. */
data class AccessorialAdjustmentQuery(
    var title: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialAdjustment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<AccessorialAdjustment>): MutableList<AccessorialAdjustment> {
        val matched = filter(items) { candidate: AccessorialAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialAdjustments(store: AccessorialAdjustmentStore, query: AccessorialAdjustmentQuery): MutableList<AccessorialAdjustment> {
    return query.apply(store.list())
}
