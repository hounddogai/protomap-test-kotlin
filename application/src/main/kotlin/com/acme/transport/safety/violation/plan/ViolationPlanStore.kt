package com.acme.transport.safety.violation.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation plans by code. */
interface ViolationPlanStore {
    fun find(code: String): ViolationPlan?
    fun save(item: ViolationPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationPlanStore(
    var items: MutableMap<String, ViolationPlan>,
    var order: MutableList<String>,
) : ViolationPlanStore {
    override fun find(code: String): ViolationPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationPlan) {
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

    override fun list(): MutableList<ViolationPlan> {
        val out = mutableListOf<ViolationPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationPlanStore(): ViolationPlanStore {
    return MemoryViolationPlanStore(mutableMapOf<String, ViolationPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationPlanStore(
    var inner: ViolationPlanStore,
    var cache: TtlCache<ViolationPlan>,
    var hits: Int,
    var misses: Int,
) : ViolationPlanStore {
    override fun find(code: String): ViolationPlan? {
        val hit = (cache.get(code) as ViolationPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationPlan> = inner.list()
}

fun newCachedViolationPlanStore(inner: ViolationPlanStore, ttl: Int, clock: Clock): ViolationPlanStore {
    return CachedViolationPlanStore(
        inner = inner,
        cache = newTtlCache<ViolationPlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation plans. Empty fields match all. */
data class ViolationPlanQuery(
    var source: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationPlan): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationPlan>): MutableList<ViolationPlan> {
        val matched = filter(items) { candidate: ViolationPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationPlans(store: ViolationPlanStore, query: ViolationPlanQuery): MutableList<ViolationPlan> {
    return query.apply(store.list())
}
