package com.acme.transport.lastmile.locker.lockertier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps strict locker plans by code. */
interface StrictLockerPlanStore {
    fun find(code: String): StrictLockerPlan?
    fun save(item: StrictLockerPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictLockerPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictLockerPlanStore(
    var items: MutableMap<String, StrictLockerPlan>,
    var order: MutableList<String>,
) : StrictLockerPlanStore {
    override fun find(code: String): StrictLockerPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictLockerPlan) {
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

    override fun list(): MutableList<StrictLockerPlan> {
        val out = mutableListOf<StrictLockerPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictLockerPlanStore(): StrictLockerPlanStore {
    return MemoryStrictLockerPlanStore(mutableMapOf<String, StrictLockerPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictLockerPlanStore(
    var inner: StrictLockerPlanStore,
    var cache: TtlCache<StrictLockerPlan>,
    var hits: Int,
    var misses: Int,
) : StrictLockerPlanStore {
    override fun find(code: String): StrictLockerPlan? {
        val hit = (cache.get(code) as StrictLockerPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictLockerPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictLockerPlan> = inner.list()
}

fun newCachedStrictLockerPlanStore(inner: StrictLockerPlanStore, ttl: Int, clock: Clock): StrictLockerPlanStore {
    return CachedStrictLockerPlanStore(
        inner = inner,
        cache = newTtlCache<StrictLockerPlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict locker plans. Empty fields match all. */
data class StrictLockerPlanQuery(
    var segment: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictLockerPlan): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictLockerPlan>): MutableList<StrictLockerPlan> {
        val matched = filter(items) { candidate: StrictLockerPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictLockerPlans(store: StrictLockerPlanStore, query: StrictLockerPlanQuery): MutableList<StrictLockerPlan> {
    return query.apply(store.list())
}
