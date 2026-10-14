package com.acme.transport.freightpay.accessorial.accessorialcycle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional accessorial cycles by code. */
interface RegionalAccessorialCycleStore {
    fun find(code: String): RegionalAccessorialCycle?
    fun save(item: RegionalAccessorialCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalAccessorialCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalAccessorialCycleStore(
    var items: MutableMap<String, RegionalAccessorialCycle>,
    var order: MutableList<String>,
) : RegionalAccessorialCycleStore {
    override fun find(code: String): RegionalAccessorialCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalAccessorialCycle) {
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

    override fun list(): MutableList<RegionalAccessorialCycle> {
        val out = mutableListOf<RegionalAccessorialCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalAccessorialCycleStore(): RegionalAccessorialCycleStore {
    return MemoryRegionalAccessorialCycleStore(
        mutableMapOf<String, RegionalAccessorialCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalAccessorialCycleStore(
    var inner: RegionalAccessorialCycleStore,
    var cache: TtlCache<RegionalAccessorialCycle>,
    var hits: Int,
    var misses: Int,
) : RegionalAccessorialCycleStore {
    override fun find(code: String): RegionalAccessorialCycle? {
        val hit = (cache.get(code) as RegionalAccessorialCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalAccessorialCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalAccessorialCycle> = inner.list()
}

fun newCachedRegionalAccessorialCycleStore(inner: RegionalAccessorialCycleStore, ttl: Int, clock: Clock): RegionalAccessorialCycleStore {
    return CachedRegionalAccessorialCycleStore(
        inner = inner,
        cache = newTtlCache<RegionalAccessorialCycle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional accessorial cycles. Empty fields match all. */
data class RegionalAccessorialCycleQuery(
    var lane: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalAccessorialCycle): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<RegionalAccessorialCycle>): MutableList<RegionalAccessorialCycle> {
        val matched = filter(items) { candidate: RegionalAccessorialCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalAccessorialCycles(store: RegionalAccessorialCycleStore, query: RegionalAccessorialCycleQuery): MutableList<RegionalAccessorialCycle> {
    return query.apply(store.list())
}
