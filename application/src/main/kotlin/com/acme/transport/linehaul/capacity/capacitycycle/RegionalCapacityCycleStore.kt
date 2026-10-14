package com.acme.transport.linehaul.capacity.capacitycycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps regional capacity cycles by code. */
interface RegionalCapacityCycleStore {
    fun find(code: String): RegionalCapacityCycle?
    fun save(item: RegionalCapacityCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCapacityCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCapacityCycleStore(
    var items: MutableMap<String, RegionalCapacityCycle>,
    var order: MutableList<String>,
) : RegionalCapacityCycleStore {
    override fun find(code: String): RegionalCapacityCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCapacityCycle) {
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

    override fun list(): MutableList<RegionalCapacityCycle> {
        val out = mutableListOf<RegionalCapacityCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCapacityCycleStore(): RegionalCapacityCycleStore {
    return MemoryRegionalCapacityCycleStore(
        mutableMapOf<String, RegionalCapacityCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCapacityCycleStore(
    var inner: RegionalCapacityCycleStore,
    var cache: TtlCache<RegionalCapacityCycle>,
    var hits: Int,
    var misses: Int,
) : RegionalCapacityCycleStore {
    override fun find(code: String): RegionalCapacityCycle? {
        val hit = (cache.get(code) as RegionalCapacityCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCapacityCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCapacityCycle> = inner.list()
}

fun newCachedRegionalCapacityCycleStore(inner: RegionalCapacityCycleStore, ttl: Int, clock: Clock): RegionalCapacityCycleStore {
    return CachedRegionalCapacityCycleStore(
        inner = inner,
        cache = newTtlCache<RegionalCapacityCycle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional capacity cycles. Empty fields match all. */
data class RegionalCapacityCycleQuery(
    var depot: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCapacityCycle): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalCapacityCycle>): MutableList<RegionalCapacityCycle> {
        val matched = filter(items) { candidate: RegionalCapacityCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCapacityCycles(store: RegionalCapacityCycleStore, query: RegionalCapacityCycleQuery): MutableList<RegionalCapacityCycle> {
    return query.apply(store.list())
}
