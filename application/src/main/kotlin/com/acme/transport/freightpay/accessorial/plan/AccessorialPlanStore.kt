package com.acme.transport.freightpay.accessorial.plan

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial plans by code. */
interface AccessorialPlanStore {
    fun find(code: String): AccessorialPlan?
    fun save(item: AccessorialPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialPlanStore(
    var items: MutableMap<String, AccessorialPlan>,
    var order: MutableList<String>,
) : AccessorialPlanStore {
    override fun find(code: String): AccessorialPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialPlan) {
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

    override fun list(): MutableList<AccessorialPlan> {
        val out = mutableListOf<AccessorialPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialPlanStore(): AccessorialPlanStore {
    return MemoryAccessorialPlanStore(mutableMapOf<String, AccessorialPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialPlanStore(
    var inner: AccessorialPlanStore,
    var cache: TtlCache<AccessorialPlan>,
    var hits: Int,
    var misses: Int,
) : AccessorialPlanStore {
    override fun find(code: String): AccessorialPlan? {
        val hit = (cache.get(code) as AccessorialPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialPlan> = inner.list()
}

fun newCachedAccessorialPlanStore(inner: AccessorialPlanStore, ttl: Int, clock: Clock): AccessorialPlanStore {
    return CachedAccessorialPlanStore(
        inner = inner,
        cache = newTtlCache<AccessorialPlan>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial plans. Empty fields match all. */
data class AccessorialPlanQuery(
    var depot: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialPlan): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AccessorialPlan>): MutableList<AccessorialPlan> {
        val matched = filter(items) { candidate: AccessorialPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialPlans(store: AccessorialPlanStore, query: AccessorialPlanQuery): MutableList<AccessorialPlan> {
    return query.apply(store.list())
}
