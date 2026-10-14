package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection cycles by code. */
interface InspectionCycleStore {
    fun find(code: String): InspectionCycle?
    fun save(item: InspectionCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionCycleStore(
    var items: MutableMap<String, InspectionCycle>,
    var order: MutableList<String>,
) : InspectionCycleStore {
    override fun find(code: String): InspectionCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionCycle) {
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

    override fun list(): MutableList<InspectionCycle> {
        val out = mutableListOf<InspectionCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionCycleStore(): InspectionCycleStore {
    return MemoryInspectionCycleStore(mutableMapOf<String, InspectionCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionCycleStore(
    var inner: InspectionCycleStore,
    var cache: TtlCache<InspectionCycle>,
    var hits: Int,
    var misses: Int,
) : InspectionCycleStore {
    override fun find(code: String): InspectionCycle? {
        val hit = (cache.get(code) as InspectionCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionCycle> = inner.list()
}

fun newCachedInspectionCycleStore(inner: InspectionCycleStore, ttl: Int, clock: Clock): InspectionCycleStore {
    return CachedInspectionCycleStore(
        inner = inner,
        cache = newTtlCache<InspectionCycle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection cycles. Empty fields match all. */
data class InspectionCycleQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionCycle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionCycle>): MutableList<InspectionCycle> {
        val matched = filter(items) { candidate: InspectionCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionCycles(store: InspectionCycleStore, query: InspectionCycleQuery): MutableList<InspectionCycle> {
    return query.apply(store.list())
}
