package com.acme.transport.safety.violation.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation adjustments by code. */
interface ViolationAdjustmentStore {
    fun find(code: String): ViolationAdjustment?
    fun save(item: ViolationAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationAdjustmentStore(
    var items: MutableMap<String, ViolationAdjustment>,
    var order: MutableList<String>,
) : ViolationAdjustmentStore {
    override fun find(code: String): ViolationAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationAdjustment) {
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

    override fun list(): MutableList<ViolationAdjustment> {
        val out = mutableListOf<ViolationAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationAdjustmentStore(): ViolationAdjustmentStore {
    return MemoryViolationAdjustmentStore(mutableMapOf<String, ViolationAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationAdjustmentStore(
    var inner: ViolationAdjustmentStore,
    var cache: TtlCache<ViolationAdjustment>,
    var hits: Int,
    var misses: Int,
) : ViolationAdjustmentStore {
    override fun find(code: String): ViolationAdjustment? {
        val hit = (cache.get(code) as ViolationAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationAdjustment> = inner.list()
}

fun newCachedViolationAdjustmentStore(inner: ViolationAdjustmentStore, ttl: Int, clock: Clock): ViolationAdjustmentStore {
    return CachedViolationAdjustmentStore(
        inner = inner,
        cache = newTtlCache<ViolationAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation adjustments. Empty fields match all. */
data class ViolationAdjustmentQuery(
    var name: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ViolationAdjustment>): MutableList<ViolationAdjustment> {
        val matched = filter(items) { candidate: ViolationAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationAdjustments(store: ViolationAdjustmentStore, query: ViolationAdjustmentQuery): MutableList<ViolationAdjustment> {
    return query.apply(store.list())
}
