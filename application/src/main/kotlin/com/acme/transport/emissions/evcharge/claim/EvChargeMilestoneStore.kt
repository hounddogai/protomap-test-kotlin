package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge milestones by code. */
interface EvChargeMilestoneStore {
    fun find(code: String): EvChargeMilestone?
    fun save(item: EvChargeMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeMilestoneStore(
    var items: MutableMap<String, EvChargeMilestone>,
    var order: MutableList<String>,
) : EvChargeMilestoneStore {
    override fun find(code: String): EvChargeMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeMilestone) {
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

    override fun list(): MutableList<EvChargeMilestone> {
        val out = mutableListOf<EvChargeMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeMilestoneStore(): EvChargeMilestoneStore {
    return MemoryEvChargeMilestoneStore(mutableMapOf<String, EvChargeMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeMilestoneStore(
    var inner: EvChargeMilestoneStore,
    var cache: TtlCache<EvChargeMilestone>,
    var hits: Int,
    var misses: Int,
) : EvChargeMilestoneStore {
    override fun find(code: String): EvChargeMilestone? {
        val hit = (cache.get(code) as EvChargeMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeMilestone> = inner.list()
}

fun newCachedEvChargeMilestoneStore(inner: EvChargeMilestoneStore, ttl: Int, clock: Clock): EvChargeMilestoneStore {
    return CachedEvChargeMilestoneStore(
        inner = inner,
        cache = newTtlCache<EvChargeMilestone>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge milestones. Empty fields match all. */
data class EvChargeMilestoneQuery(
    var depot: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeMilestone): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeMilestone>): MutableList<EvChargeMilestone> {
        val matched = filter(items) { candidate: EvChargeMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeMilestones(store: EvChargeMilestoneStore, query: EvChargeMilestoneQuery): MutableList<EvChargeMilestone> {
    return query.apply(store.list())
}
