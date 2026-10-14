package com.acme.transport.terminal.crossdock.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock plans by code. */
interface CrossDockPlanStore {
    fun find(code: String): CrossDockPlan?
    fun save(item: CrossDockPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockPlanStore(
    var items: MutableMap<String, CrossDockPlan>,
    var order: MutableList<String>,
) : CrossDockPlanStore {
    override fun find(code: String): CrossDockPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockPlan) {
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

    override fun list(): MutableList<CrossDockPlan> {
        val out = mutableListOf<CrossDockPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockPlanStore(): CrossDockPlanStore {
    return MemoryCrossDockPlanStore(mutableMapOf<String, CrossDockPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockPlanStore(
    var inner: CrossDockPlanStore,
    var cache: TtlCache<CrossDockPlan>,
    var hits: Int,
    var misses: Int,
) : CrossDockPlanStore {
    override fun find(code: String): CrossDockPlan? {
        val hit = (cache.get(code) as CrossDockPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockPlan> = inner.list()
}

fun newCachedCrossDockPlanStore(inner: CrossDockPlanStore, ttl: Int, clock: Clock): CrossDockPlanStore {
    return CachedCrossDockPlanStore(
        inner = inner,
        cache = newTtlCache<CrossDockPlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock plans. Empty fields match all. */
data class CrossDockPlanQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockPlan): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<CrossDockPlan>): MutableList<CrossDockPlan> {
        val matched = filter(items) { candidate: CrossDockPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockPlans(store: CrossDockPlanStore, query: CrossDockPlanQuery): MutableList<CrossDockPlan> {
    return query.apply(store.list())
}
