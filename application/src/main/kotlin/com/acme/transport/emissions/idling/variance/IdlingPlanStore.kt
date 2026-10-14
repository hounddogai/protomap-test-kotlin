package com.acme.transport.emissions.idling.variance

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling plans by code. */
interface IdlingPlanStore {
    fun find(code: String): IdlingPlan?
    fun save(item: IdlingPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingPlanStore(
    var items: MutableMap<String, IdlingPlan>,
    var order: MutableList<String>,
) : IdlingPlanStore {
    override fun find(code: String): IdlingPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingPlan) {
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

    override fun list(): MutableList<IdlingPlan> {
        val out = mutableListOf<IdlingPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingPlanStore(): IdlingPlanStore {
    return MemoryIdlingPlanStore(mutableMapOf<String, IdlingPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingPlanStore(
    var inner: IdlingPlanStore,
    var cache: TtlCache<IdlingPlan>,
    var hits: Int,
    var misses: Int,
) : IdlingPlanStore {
    override fun find(code: String): IdlingPlan? {
        val hit = (cache.get(code) as IdlingPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingPlan> = inner.list()
}

fun newCachedIdlingPlanStore(inner: IdlingPlanStore, ttl: Int, clock: Clock): IdlingPlanStore {
    return CachedIdlingPlanStore(
        inner = inner,
        cache = newTtlCache<IdlingPlan>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling plans. Empty fields match all. */
data class IdlingPlanQuery(
    var category: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingPlan): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingPlan>): MutableList<IdlingPlan> {
        val matched = filter(items) { candidate: IdlingPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingPlans(store: IdlingPlanStore, query: IdlingPlanQuery): MutableList<IdlingPlan> {
    return query.apply(store.list())
}
