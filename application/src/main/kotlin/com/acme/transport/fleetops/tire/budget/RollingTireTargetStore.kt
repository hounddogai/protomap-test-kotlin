package com.acme.transport.fleetops.tire.budget

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling tire targets by code. */
interface RollingTireTargetStore {
    fun find(code: String): RollingTireTarget?
    fun save(item: RollingTireTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingTireTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingTireTargetStore(
    var items: MutableMap<String, RollingTireTarget>,
    var order: MutableList<String>,
) : RollingTireTargetStore {
    override fun find(code: String): RollingTireTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingTireTarget) {
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

    override fun list(): MutableList<RollingTireTarget> {
        val out = mutableListOf<RollingTireTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingTireTargetStore(): RollingTireTargetStore {
    return MemoryRollingTireTargetStore(mutableMapOf<String, RollingTireTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingTireTargetStore(
    var inner: RollingTireTargetStore,
    var cache: TtlCache<RollingTireTarget>,
    var hits: Int,
    var misses: Int,
) : RollingTireTargetStore {
    override fun find(code: String): RollingTireTarget? {
        val hit = (cache.get(code) as RollingTireTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingTireTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingTireTarget> = inner.list()
}

fun newCachedRollingTireTargetStore(inner: RollingTireTargetStore, ttl: Int, clock: Clock): RollingTireTargetStore {
    return CachedRollingTireTargetStore(
        inner = inner,
        cache = newTtlCache<RollingTireTarget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling tire targets. Empty fields match all. */
data class RollingTireTargetQuery(
    var source: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingTireTarget): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingTireTarget>): MutableList<RollingTireTarget> {
        val matched = filter(items) { candidate: RollingTireTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingTireTargets(store: RollingTireTargetStore, query: RollingTireTargetQuery): MutableList<RollingTireTarget> {
    return query.apply(store.list())
}
