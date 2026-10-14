package com.acme.transport.lastmile.locker.lockerprofile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps fallback locker plans by code. */
interface FallbackLockerPlanStore {
    fun find(code: String): FallbackLockerPlan?
    fun save(item: FallbackLockerPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackLockerPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackLockerPlanStore(
    var items: MutableMap<String, FallbackLockerPlan>,
    var order: MutableList<String>,
) : FallbackLockerPlanStore {
    override fun find(code: String): FallbackLockerPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackLockerPlan) {
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

    override fun list(): MutableList<FallbackLockerPlan> {
        val out = mutableListOf<FallbackLockerPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackLockerPlanStore(): FallbackLockerPlanStore {
    return MemoryFallbackLockerPlanStore(mutableMapOf<String, FallbackLockerPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackLockerPlanStore(
    var inner: FallbackLockerPlanStore,
    var cache: TtlCache<FallbackLockerPlan>,
    var hits: Int,
    var misses: Int,
) : FallbackLockerPlanStore {
    override fun find(code: String): FallbackLockerPlan? {
        val hit = (cache.get(code) as FallbackLockerPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackLockerPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackLockerPlan> = inner.list()
}

fun newCachedFallbackLockerPlanStore(inner: FallbackLockerPlanStore, ttl: Int, clock: Clock): FallbackLockerPlanStore {
    return CachedFallbackLockerPlanStore(
        inner = inner,
        cache = newTtlCache<FallbackLockerPlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback locker plans. Empty fields match all. */
data class FallbackLockerPlanQuery(
    var title: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackLockerPlan): Boolean {
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

    fun apply(items: MutableList<FallbackLockerPlan>): MutableList<FallbackLockerPlan> {
        val matched = filter(items) { candidate: FallbackLockerPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackLockerPlans(store: FallbackLockerPlanStore, query: FallbackLockerPlanQuery): MutableList<FallbackLockerPlan> {
    return query.apply(store.list())
}
