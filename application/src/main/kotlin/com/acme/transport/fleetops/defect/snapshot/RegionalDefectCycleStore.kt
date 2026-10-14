package com.acme.transport.fleetops.defect.snapshot

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional defect cycles by code. */
interface RegionalDefectCycleStore {
    fun find(code: String): RegionalDefectCycle?
    fun save(item: RegionalDefectCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDefectCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDefectCycleStore(
    var items: MutableMap<String, RegionalDefectCycle>,
    var order: MutableList<String>,
) : RegionalDefectCycleStore {
    override fun find(code: String): RegionalDefectCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDefectCycle) {
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

    override fun list(): MutableList<RegionalDefectCycle> {
        val out = mutableListOf<RegionalDefectCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDefectCycleStore(): RegionalDefectCycleStore {
    return MemoryRegionalDefectCycleStore(mutableMapOf<String, RegionalDefectCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDefectCycleStore(
    var inner: RegionalDefectCycleStore,
    var cache: TtlCache<RegionalDefectCycle>,
    var hits: Int,
    var misses: Int,
) : RegionalDefectCycleStore {
    override fun find(code: String): RegionalDefectCycle? {
        val hit = (cache.get(code) as RegionalDefectCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDefectCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDefectCycle> = inner.list()
}

fun newCachedRegionalDefectCycleStore(inner: RegionalDefectCycleStore, ttl: Int, clock: Clock): RegionalDefectCycleStore {
    return CachedRegionalDefectCycleStore(
        inner = inner,
        cache = newTtlCache<RegionalDefectCycle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional defect cycles. Empty fields match all. */
data class RegionalDefectCycleQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDefectCycle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalDefectCycle>): MutableList<RegionalDefectCycle> {
        val matched = filter(items) { candidate: RegionalDefectCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDefectCycles(store: RegionalDefectCycleStore, query: RegionalDefectCycleQuery): MutableList<RegionalDefectCycle> {
    return query.apply(store.list())
}
