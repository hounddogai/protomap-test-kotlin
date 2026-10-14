package com.acme.transport.linehaul.capacity.capacitytarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps regional capacity targets by code. */
interface RegionalCapacityTargetStore {
    fun find(code: String): RegionalCapacityTarget?
    fun save(item: RegionalCapacityTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCapacityTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCapacityTargetStore(
    var items: MutableMap<String, RegionalCapacityTarget>,
    var order: MutableList<String>,
) : RegionalCapacityTargetStore {
    override fun find(code: String): RegionalCapacityTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCapacityTarget) {
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

    override fun list(): MutableList<RegionalCapacityTarget> {
        val out = mutableListOf<RegionalCapacityTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCapacityTargetStore(): RegionalCapacityTargetStore {
    return MemoryRegionalCapacityTargetStore(
        mutableMapOf<String, RegionalCapacityTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCapacityTargetStore(
    var inner: RegionalCapacityTargetStore,
    var cache: TtlCache<RegionalCapacityTarget>,
    var hits: Int,
    var misses: Int,
) : RegionalCapacityTargetStore {
    override fun find(code: String): RegionalCapacityTarget? {
        val hit = (cache.get(code) as RegionalCapacityTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCapacityTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCapacityTarget> = inner.list()
}

fun newCachedRegionalCapacityTargetStore(inner: RegionalCapacityTargetStore, ttl: Int, clock: Clock): RegionalCapacityTargetStore {
    return CachedRegionalCapacityTargetStore(
        inner = inner,
        cache = newTtlCache<RegionalCapacityTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional capacity targets. Empty fields match all. */
data class RegionalCapacityTargetQuery(
    var unit: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCapacityTarget): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalCapacityTarget>): MutableList<RegionalCapacityTarget> {
        val matched = filter(items) { candidate: RegionalCapacityTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCapacityTargets(store: RegionalCapacityTargetStore, query: RegionalCapacityTargetQuery): MutableList<RegionalCapacityTarget> {
    return query.apply(store.list())
}
