package com.acme.transport.terminal.crossdock.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps global cross dock plans by code. */
interface GlobalCrossDockPlanStore {
    fun find(code: String): GlobalCrossDockPlan?
    fun save(item: GlobalCrossDockPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCrossDockPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCrossDockPlanStore(
    var items: MutableMap<String, GlobalCrossDockPlan>,
    var order: MutableList<String>,
) : GlobalCrossDockPlanStore {
    override fun find(code: String): GlobalCrossDockPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCrossDockPlan) {
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

    override fun list(): MutableList<GlobalCrossDockPlan> {
        val out = mutableListOf<GlobalCrossDockPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCrossDockPlanStore(): GlobalCrossDockPlanStore {
    return MemoryGlobalCrossDockPlanStore(mutableMapOf<String, GlobalCrossDockPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCrossDockPlanStore(
    var inner: GlobalCrossDockPlanStore,
    var cache: TtlCache<GlobalCrossDockPlan>,
    var hits: Int,
    var misses: Int,
) : GlobalCrossDockPlanStore {
    override fun find(code: String): GlobalCrossDockPlan? {
        val hit = (cache.get(code) as GlobalCrossDockPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCrossDockPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCrossDockPlan> = inner.list()
}

fun newCachedGlobalCrossDockPlanStore(inner: GlobalCrossDockPlanStore, ttl: Int, clock: Clock): GlobalCrossDockPlanStore {
    return CachedGlobalCrossDockPlanStore(
        inner = inner,
        cache = newTtlCache<GlobalCrossDockPlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global cross dock plans. Empty fields match all. */
data class GlobalCrossDockPlanQuery(
    var label: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCrossDockPlan): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<GlobalCrossDockPlan>): MutableList<GlobalCrossDockPlan> {
        val matched = filter(items) { candidate: GlobalCrossDockPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCrossDockPlans(store: GlobalCrossDockPlanStore, query: GlobalCrossDockPlanQuery): MutableList<GlobalCrossDockPlan> {
    return query.apply(store.list())
}
