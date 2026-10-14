package com.acme.transport.emissions.fuelburn.burnestimate

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped fuel burn milestones by code. */
interface CappedFuelBurnMilestoneStore {
    fun find(code: String): CappedFuelBurnMilestone?
    fun save(item: CappedFuelBurnMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedFuelBurnMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedFuelBurnMilestoneStore(
    var items: MutableMap<String, CappedFuelBurnMilestone>,
    var order: MutableList<String>,
) : CappedFuelBurnMilestoneStore {
    override fun find(code: String): CappedFuelBurnMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedFuelBurnMilestone) {
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

    override fun list(): MutableList<CappedFuelBurnMilestone> {
        val out = mutableListOf<CappedFuelBurnMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedFuelBurnMilestoneStore(): CappedFuelBurnMilestoneStore {
    return MemoryCappedFuelBurnMilestoneStore(
        mutableMapOf<String, CappedFuelBurnMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedFuelBurnMilestoneStore(
    var inner: CappedFuelBurnMilestoneStore,
    var cache: TtlCache<CappedFuelBurnMilestone>,
    var hits: Int,
    var misses: Int,
) : CappedFuelBurnMilestoneStore {
    override fun find(code: String): CappedFuelBurnMilestone? {
        val hit = (cache.get(code) as CappedFuelBurnMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedFuelBurnMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedFuelBurnMilestone> = inner.list()
}

fun newCachedCappedFuelBurnMilestoneStore(inner: CappedFuelBurnMilestoneStore, ttl: Int, clock: Clock): CappedFuelBurnMilestoneStore {
    return CachedCappedFuelBurnMilestoneStore(
        inner = inner,
        cache = newTtlCache<CappedFuelBurnMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped fuel burn milestones. Empty fields match all. */
data class CappedFuelBurnMilestoneQuery(
    var title: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedFuelBurnMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedFuelBurnMilestone>): MutableList<CappedFuelBurnMilestone> {
        val matched = filter(items) { candidate: CappedFuelBurnMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedFuelBurnMilestones(store: CappedFuelBurnMilestoneStore, query: CappedFuelBurnMilestoneQuery): MutableList<CappedFuelBurnMilestone> {
    return query.apply(store.list())
}
