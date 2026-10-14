package com.acme.transport.linehaul.laneplan.plancheckpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps regional lane plan cycles by code. */
interface RegionalLanePlanCycleStore {
    fun find(code: String): RegionalLanePlanCycle?
    fun save(item: RegionalLanePlanCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalLanePlanCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalLanePlanCycleStore(
    var items: MutableMap<String, RegionalLanePlanCycle>,
    var order: MutableList<String>,
) : RegionalLanePlanCycleStore {
    override fun find(code: String): RegionalLanePlanCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalLanePlanCycle) {
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

    override fun list(): MutableList<RegionalLanePlanCycle> {
        val out = mutableListOf<RegionalLanePlanCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalLanePlanCycleStore(): RegionalLanePlanCycleStore {
    return MemoryRegionalLanePlanCycleStore(
        mutableMapOf<String, RegionalLanePlanCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalLanePlanCycleStore(
    var inner: RegionalLanePlanCycleStore,
    var cache: TtlCache<RegionalLanePlanCycle>,
    var hits: Int,
    var misses: Int,
) : RegionalLanePlanCycleStore {
    override fun find(code: String): RegionalLanePlanCycle? {
        val hit = (cache.get(code) as RegionalLanePlanCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalLanePlanCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalLanePlanCycle> = inner.list()
}

fun newCachedRegionalLanePlanCycleStore(inner: RegionalLanePlanCycleStore, ttl: Int, clock: Clock): RegionalLanePlanCycleStore {
    return CachedRegionalLanePlanCycleStore(
        inner = inner,
        cache = newTtlCache<RegionalLanePlanCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional lane plan cycles. Empty fields match all. */
data class RegionalLanePlanCycleQuery(
    var channel: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalLanePlanCycle): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<RegionalLanePlanCycle>): MutableList<RegionalLanePlanCycle> {
        val matched = filter(items) { candidate: RegionalLanePlanCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalLanePlanCycles(store: RegionalLanePlanCycleStore, query: RegionalLanePlanCycleQuery): MutableList<RegionalLanePlanCycle> {
    return query.apply(store.list())
}
