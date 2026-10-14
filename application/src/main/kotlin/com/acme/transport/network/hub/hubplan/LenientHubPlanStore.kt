package com.acme.transport.network.hub.hubplan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient hub plans by code. */
interface LenientHubPlanStore {
    fun find(code: String): LenientHubPlan?
    fun save(item: LenientHubPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientHubPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientHubPlanStore(
    var items: MutableMap<String, LenientHubPlan>,
    var order: MutableList<String>,
) : LenientHubPlanStore {
    override fun find(code: String): LenientHubPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientHubPlan) {
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

    override fun list(): MutableList<LenientHubPlan> {
        val out = mutableListOf<LenientHubPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientHubPlanStore(): LenientHubPlanStore {
    return MemoryLenientHubPlanStore(mutableMapOf<String, LenientHubPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientHubPlanStore(
    var inner: LenientHubPlanStore,
    var cache: TtlCache<LenientHubPlan>,
    var hits: Int,
    var misses: Int,
) : LenientHubPlanStore {
    override fun find(code: String): LenientHubPlan? {
        val hit = (cache.get(code) as LenientHubPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientHubPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientHubPlan> = inner.list()
}

fun newCachedLenientHubPlanStore(inner: LenientHubPlanStore, ttl: Int, clock: Clock): LenientHubPlanStore {
    return CachedLenientHubPlanStore(
        inner = inner,
        cache = newTtlCache<LenientHubPlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient hub plans. Empty fields match all. */
data class LenientHubPlanQuery(
    var category: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientHubPlan): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<LenientHubPlan>): MutableList<LenientHubPlan> {
        val matched = filter(items) { candidate: LenientHubPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientHubPlans(store: LenientHubPlanStore, query: LenientHubPlanQuery): MutableList<LenientHubPlan> {
    return query.apply(store.list())
}
