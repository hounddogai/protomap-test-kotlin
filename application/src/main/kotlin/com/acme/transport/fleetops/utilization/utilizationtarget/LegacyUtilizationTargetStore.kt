package com.acme.transport.fleetops.utilization.utilizationtarget

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy utilization targets by code. */
interface LegacyUtilizationTargetStore {
    fun find(code: String): LegacyUtilizationTarget?
    fun save(item: LegacyUtilizationTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyUtilizationTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyUtilizationTargetStore(
    var items: MutableMap<String, LegacyUtilizationTarget>,
    var order: MutableList<String>,
) : LegacyUtilizationTargetStore {
    override fun find(code: String): LegacyUtilizationTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyUtilizationTarget) {
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

    override fun list(): MutableList<LegacyUtilizationTarget> {
        val out = mutableListOf<LegacyUtilizationTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyUtilizationTargetStore(): LegacyUtilizationTargetStore {
    return MemoryLegacyUtilizationTargetStore(
        mutableMapOf<String, LegacyUtilizationTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyUtilizationTargetStore(
    var inner: LegacyUtilizationTargetStore,
    var cache: TtlCache<LegacyUtilizationTarget>,
    var hits: Int,
    var misses: Int,
) : LegacyUtilizationTargetStore {
    override fun find(code: String): LegacyUtilizationTarget? {
        val hit = (cache.get(code) as LegacyUtilizationTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyUtilizationTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyUtilizationTarget> = inner.list()
}

fun newCachedLegacyUtilizationTargetStore(inner: LegacyUtilizationTargetStore, ttl: Int, clock: Clock): LegacyUtilizationTargetStore {
    return CachedLegacyUtilizationTargetStore(
        inner = inner,
        cache = newTtlCache<LegacyUtilizationTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy utilization targets. Empty fields match all. */
data class LegacyUtilizationTargetQuery(
    var depot: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyUtilizationTarget): Boolean {
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

    fun apply(items: MutableList<LegacyUtilizationTarget>): MutableList<LegacyUtilizationTarget> {
        val matched = filter(items) { candidate: LegacyUtilizationTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyUtilizationTargets(store: LegacyUtilizationTargetStore, query: LegacyUtilizationTargetQuery): MutableList<LegacyUtilizationTarget> {
    return query.apply(store.list())
}
