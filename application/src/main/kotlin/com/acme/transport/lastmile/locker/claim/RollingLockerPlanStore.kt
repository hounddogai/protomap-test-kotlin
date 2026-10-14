package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps rolling locker plans by code. */
interface RollingLockerPlanStore {
    fun find(code: String): RollingLockerPlan?
    fun save(item: RollingLockerPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingLockerPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingLockerPlanStore(
    var items: MutableMap<String, RollingLockerPlan>,
    var order: MutableList<String>,
) : RollingLockerPlanStore {
    override fun find(code: String): RollingLockerPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingLockerPlan) {
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

    override fun list(): MutableList<RollingLockerPlan> {
        val out = mutableListOf<RollingLockerPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingLockerPlanStore(): RollingLockerPlanStore {
    return MemoryRollingLockerPlanStore(mutableMapOf<String, RollingLockerPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingLockerPlanStore(
    var inner: RollingLockerPlanStore,
    var cache: TtlCache<RollingLockerPlan>,
    var hits: Int,
    var misses: Int,
) : RollingLockerPlanStore {
    override fun find(code: String): RollingLockerPlan? {
        val hit = (cache.get(code) as RollingLockerPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingLockerPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingLockerPlan> = inner.list()
}

fun newCachedRollingLockerPlanStore(inner: RollingLockerPlanStore, ttl: Int, clock: Clock): RollingLockerPlanStore {
    return CachedRollingLockerPlanStore(
        inner = inner,
        cache = newTtlCache<RollingLockerPlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling locker plans. Empty fields match all. */
data class RollingLockerPlanQuery(
    var segment: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingLockerPlan): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<RollingLockerPlan>): MutableList<RollingLockerPlan> {
        val matched = filter(items) { candidate: RollingLockerPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingLockerPlans(store: RollingLockerPlanStore, query: RollingLockerPlanQuery): MutableList<RollingLockerPlan> {
    return query.apply(store.list())
}
