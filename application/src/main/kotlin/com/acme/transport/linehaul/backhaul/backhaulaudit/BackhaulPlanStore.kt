package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul plans by code. */
interface BackhaulPlanStore {
    fun find(code: String): BackhaulPlan?
    fun save(item: BackhaulPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulPlanStore(
    var items: MutableMap<String, BackhaulPlan>,
    var order: MutableList<String>,
) : BackhaulPlanStore {
    override fun find(code: String): BackhaulPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulPlan) {
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

    override fun list(): MutableList<BackhaulPlan> {
        val out = mutableListOf<BackhaulPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulPlanStore(): BackhaulPlanStore {
    return MemoryBackhaulPlanStore(mutableMapOf<String, BackhaulPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulPlanStore(
    var inner: BackhaulPlanStore,
    var cache: TtlCache<BackhaulPlan>,
    var hits: Int,
    var misses: Int,
) : BackhaulPlanStore {
    override fun find(code: String): BackhaulPlan? {
        val hit = (cache.get(code) as BackhaulPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulPlan> = inner.list()
}

fun newCachedBackhaulPlanStore(inner: BackhaulPlanStore, ttl: Int, clock: Clock): BackhaulPlanStore {
    return CachedBackhaulPlanStore(
        inner = inner,
        cache = newTtlCache<BackhaulPlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul plans. Empty fields match all. */
data class BackhaulPlanQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulPlan): Boolean {
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

    fun apply(items: MutableList<BackhaulPlan>): MutableList<BackhaulPlan> {
        val matched = filter(items) { candidate: BackhaulPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulPlans(store: BackhaulPlanStore, query: BackhaulPlanQuery): MutableList<BackhaulPlan> {
    return query.apply(store.list())
}
