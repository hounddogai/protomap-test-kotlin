package com.acme.transport.lastmile.locker.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker estimates by code. */
interface LockerEstimateStore {
    fun find(code: String): LockerEstimate?
    fun save(item: LockerEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerEstimateStore(
    var items: MutableMap<String, LockerEstimate>,
    var order: MutableList<String>,
) : LockerEstimateStore {
    override fun find(code: String): LockerEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerEstimate) {
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

    override fun list(): MutableList<LockerEstimate> {
        val out = mutableListOf<LockerEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerEstimateStore(): LockerEstimateStore {
    return MemoryLockerEstimateStore(mutableMapOf<String, LockerEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerEstimateStore(
    var inner: LockerEstimateStore,
    var cache: TtlCache<LockerEstimate>,
    var hits: Int,
    var misses: Int,
) : LockerEstimateStore {
    override fun find(code: String): LockerEstimate? {
        val hit = (cache.get(code) as LockerEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerEstimate> = inner.list()
}

fun newCachedLockerEstimateStore(inner: LockerEstimateStore, ttl: Int, clock: Clock): LockerEstimateStore {
    return CachedLockerEstimateStore(
        inner = inner,
        cache = newTtlCache<LockerEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker estimates. Empty fields match all. */
data class LockerEstimateQuery(
    var label: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerEstimate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerEstimate>): MutableList<LockerEstimate> {
        val matched = filter(items) { candidate: LockerEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerEstimates(store: LockerEstimateStore, query: LockerEstimateQuery): MutableList<LockerEstimate> {
    return query.apply(store.list())
}
