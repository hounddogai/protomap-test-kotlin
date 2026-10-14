package com.acme.transport.lastmile.locker.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default locker plans by code. */
interface DefaultLockerPlanStore {
    fun find(code: String): DefaultLockerPlan?
    fun save(item: DefaultLockerPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultLockerPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultLockerPlanStore(
    var items: MutableMap<String, DefaultLockerPlan>,
    var order: MutableList<String>,
) : DefaultLockerPlanStore {
    override fun find(code: String): DefaultLockerPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultLockerPlan) {
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

    override fun list(): MutableList<DefaultLockerPlan> {
        val out = mutableListOf<DefaultLockerPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultLockerPlanStore(): DefaultLockerPlanStore {
    return MemoryDefaultLockerPlanStore(mutableMapOf<String, DefaultLockerPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultLockerPlanStore(
    var inner: DefaultLockerPlanStore,
    var cache: TtlCache<DefaultLockerPlan>,
    var hits: Int,
    var misses: Int,
) : DefaultLockerPlanStore {
    override fun find(code: String): DefaultLockerPlan? {
        val hit = (cache.get(code) as DefaultLockerPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultLockerPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultLockerPlan> = inner.list()
}

fun newCachedDefaultLockerPlanStore(inner: DefaultLockerPlanStore, ttl: Int, clock: Clock): DefaultLockerPlanStore {
    return CachedDefaultLockerPlanStore(
        inner = inner,
        cache = newTtlCache<DefaultLockerPlan>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default locker plans. Empty fields match all. */
data class DefaultLockerPlanQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultLockerPlan): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultLockerPlan>): MutableList<DefaultLockerPlan> {
        val matched = filter(items) { candidate: DefaultLockerPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultLockerPlans(store: DefaultLockerPlanStore, query: DefaultLockerPlanQuery): MutableList<DefaultLockerPlan> {
    return query.apply(store.list())
}
