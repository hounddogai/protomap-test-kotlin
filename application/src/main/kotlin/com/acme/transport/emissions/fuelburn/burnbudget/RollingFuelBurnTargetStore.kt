package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling fuel burn targets by code. */
interface RollingFuelBurnTargetStore {
    fun find(code: String): RollingFuelBurnTarget?
    fun save(item: RollingFuelBurnTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingFuelBurnTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingFuelBurnTargetStore(
    var items: MutableMap<String, RollingFuelBurnTarget>,
    var order: MutableList<String>,
) : RollingFuelBurnTargetStore {
    override fun find(code: String): RollingFuelBurnTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingFuelBurnTarget) {
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

    override fun list(): MutableList<RollingFuelBurnTarget> {
        val out = mutableListOf<RollingFuelBurnTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingFuelBurnTargetStore(): RollingFuelBurnTargetStore {
    return MemoryRollingFuelBurnTargetStore(
        mutableMapOf<String, RollingFuelBurnTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingFuelBurnTargetStore(
    var inner: RollingFuelBurnTargetStore,
    var cache: TtlCache<RollingFuelBurnTarget>,
    var hits: Int,
    var misses: Int,
) : RollingFuelBurnTargetStore {
    override fun find(code: String): RollingFuelBurnTarget? {
        val hit = (cache.get(code) as RollingFuelBurnTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingFuelBurnTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingFuelBurnTarget> = inner.list()
}

fun newCachedRollingFuelBurnTargetStore(inner: RollingFuelBurnTargetStore, ttl: Int, clock: Clock): RollingFuelBurnTargetStore {
    return CachedRollingFuelBurnTargetStore(
        inner = inner,
        cache = newTtlCache<RollingFuelBurnTarget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling fuel burn targets. Empty fields match all. */
data class RollingFuelBurnTargetQuery(
    var region: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingFuelBurnTarget): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<RollingFuelBurnTarget>): MutableList<RollingFuelBurnTarget> {
        val matched = filter(items) { candidate: RollingFuelBurnTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingFuelBurnTargets(store: RollingFuelBurnTargetStore, query: RollingFuelBurnTargetQuery): MutableList<RollingFuelBurnTarget> {
    return query.apply(store.list())
}
