package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global service plan targets by code. */
interface GlobalServicePlanTargetStore {
    fun find(code: String): GlobalServicePlanTarget?
    fun save(item: GlobalServicePlanTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalServicePlanTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalServicePlanTargetStore(
    var items: MutableMap<String, GlobalServicePlanTarget>,
    var order: MutableList<String>,
) : GlobalServicePlanTargetStore {
    override fun find(code: String): GlobalServicePlanTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalServicePlanTarget) {
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

    override fun list(): MutableList<GlobalServicePlanTarget> {
        val out = mutableListOf<GlobalServicePlanTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalServicePlanTargetStore(): GlobalServicePlanTargetStore {
    return MemoryGlobalServicePlanTargetStore(
        mutableMapOf<String, GlobalServicePlanTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalServicePlanTargetStore(
    var inner: GlobalServicePlanTargetStore,
    var cache: TtlCache<GlobalServicePlanTarget>,
    var hits: Int,
    var misses: Int,
) : GlobalServicePlanTargetStore {
    override fun find(code: String): GlobalServicePlanTarget? {
        val hit = (cache.get(code) as GlobalServicePlanTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalServicePlanTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalServicePlanTarget> = inner.list()
}

fun newCachedGlobalServicePlanTargetStore(inner: GlobalServicePlanTargetStore, ttl: Int, clock: Clock): GlobalServicePlanTargetStore {
    return CachedGlobalServicePlanTargetStore(
        inner = inner,
        cache = newTtlCache<GlobalServicePlanTarget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global service plan targets. Empty fields match all. */
data class GlobalServicePlanTargetQuery(
    var depot: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalServicePlanTarget): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalServicePlanTarget>): MutableList<GlobalServicePlanTarget> {
        val matched = filter(items) { candidate: GlobalServicePlanTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalServicePlanTargets(store: GlobalServicePlanTargetStore, query: GlobalServicePlanTargetQuery): MutableList<GlobalServicePlanTarget> {
    return query.apply(store.list())
}
