package com.acme.transport.linehaul.capacity.capacitytarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps rolling capacity cycles by code. */
interface RollingCapacityCycleStore {
    fun find(code: String): RollingCapacityCycle?
    fun save(item: RollingCapacityCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingCapacityCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingCapacityCycleStore(
    var items: MutableMap<String, RollingCapacityCycle>,
    var order: MutableList<String>,
) : RollingCapacityCycleStore {
    override fun find(code: String): RollingCapacityCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingCapacityCycle) {
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

    override fun list(): MutableList<RollingCapacityCycle> {
        val out = mutableListOf<RollingCapacityCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingCapacityCycleStore(): RollingCapacityCycleStore {
    return MemoryRollingCapacityCycleStore(
        mutableMapOf<String, RollingCapacityCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingCapacityCycleStore(
    var inner: RollingCapacityCycleStore,
    var cache: TtlCache<RollingCapacityCycle>,
    var hits: Int,
    var misses: Int,
) : RollingCapacityCycleStore {
    override fun find(code: String): RollingCapacityCycle? {
        val hit = (cache.get(code) as RollingCapacityCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingCapacityCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingCapacityCycle> = inner.list()
}

fun newCachedRollingCapacityCycleStore(inner: RollingCapacityCycleStore, ttl: Int, clock: Clock): RollingCapacityCycleStore {
    return CachedRollingCapacityCycleStore(
        inner = inner,
        cache = newTtlCache<RollingCapacityCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling capacity cycles. Empty fields match all. */
data class RollingCapacityCycleQuery(
    var region: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingCapacityCycle): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingCapacityCycle>): MutableList<RollingCapacityCycle> {
        val matched = filter(items) { candidate: RollingCapacityCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingCapacityCycles(store: RollingCapacityCycleStore, query: RollingCapacityCycleQuery): MutableList<RollingCapacityCycle> {
    return query.apply(store.list())
}
