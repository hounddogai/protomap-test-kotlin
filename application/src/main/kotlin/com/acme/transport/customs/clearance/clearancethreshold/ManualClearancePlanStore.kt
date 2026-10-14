package com.acme.transport.customs.clearance.clearancethreshold

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual clearance plans by code. */
interface ManualClearancePlanStore {
    fun find(code: String): ManualClearancePlan?
    fun save(item: ManualClearancePlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualClearancePlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualClearancePlanStore(
    var items: MutableMap<String, ManualClearancePlan>,
    var order: MutableList<String>,
) : ManualClearancePlanStore {
    override fun find(code: String): ManualClearancePlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualClearancePlan) {
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

    override fun list(): MutableList<ManualClearancePlan> {
        val out = mutableListOf<ManualClearancePlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualClearancePlanStore(): ManualClearancePlanStore {
    return MemoryManualClearancePlanStore(mutableMapOf<String, ManualClearancePlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualClearancePlanStore(
    var inner: ManualClearancePlanStore,
    var cache: TtlCache<ManualClearancePlan>,
    var hits: Int,
    var misses: Int,
) : ManualClearancePlanStore {
    override fun find(code: String): ManualClearancePlan? {
        val hit = (cache.get(code) as ManualClearancePlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualClearancePlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualClearancePlan> = inner.list()
}

fun newCachedManualClearancePlanStore(inner: ManualClearancePlanStore, ttl: Int, clock: Clock): ManualClearancePlanStore {
    return CachedManualClearancePlanStore(
        inner = inner,
        cache = newTtlCache<ManualClearancePlan>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual clearance plans. Empty fields match all. */
data class ManualClearancePlanQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualClearancePlan): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualClearancePlan>): MutableList<ManualClearancePlan> {
        val matched = filter(items) { candidate: ManualClearancePlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualClearancePlans(store: ManualClearancePlanStore, query: ManualClearancePlanQuery): MutableList<ManualClearancePlan> {
    return query.apply(store.list())
}
