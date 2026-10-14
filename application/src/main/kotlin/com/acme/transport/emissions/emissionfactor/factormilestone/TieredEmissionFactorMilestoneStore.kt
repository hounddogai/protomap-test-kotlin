package com.acme.transport.emissions.emissionfactor.factormilestone

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered emission factor milestones by code. */
interface TieredEmissionFactorMilestoneStore {
    fun find(code: String): TieredEmissionFactorMilestone?
    fun save(item: TieredEmissionFactorMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredEmissionFactorMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredEmissionFactorMilestoneStore(
    var items: MutableMap<String, TieredEmissionFactorMilestone>,
    var order: MutableList<String>,
) : TieredEmissionFactorMilestoneStore {
    override fun find(code: String): TieredEmissionFactorMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredEmissionFactorMilestone) {
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

    override fun list(): MutableList<TieredEmissionFactorMilestone> {
        val out = mutableListOf<TieredEmissionFactorMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredEmissionFactorMilestoneStore(): TieredEmissionFactorMilestoneStore {
    return MemoryTieredEmissionFactorMilestoneStore(
        mutableMapOf<String, TieredEmissionFactorMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredEmissionFactorMilestoneStore(
    var inner: TieredEmissionFactorMilestoneStore,
    var cache: TtlCache<TieredEmissionFactorMilestone>,
    var hits: Int,
    var misses: Int,
) : TieredEmissionFactorMilestoneStore {
    override fun find(code: String): TieredEmissionFactorMilestone? {
        val hit = (cache.get(code) as TieredEmissionFactorMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredEmissionFactorMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredEmissionFactorMilestone> = inner.list()
}

fun newCachedTieredEmissionFactorMilestoneStore(inner: TieredEmissionFactorMilestoneStore, ttl: Int, clock: Clock): TieredEmissionFactorMilestoneStore {
    return CachedTieredEmissionFactorMilestoneStore(
        inner = inner,
        cache = newTtlCache<TieredEmissionFactorMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered emission factor milestones. Empty fields match all. */
data class TieredEmissionFactorMilestoneQuery(
    var source: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredEmissionFactorMilestone): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredEmissionFactorMilestone>): MutableList<TieredEmissionFactorMilestone> {
        val matched = filter(items) { candidate: TieredEmissionFactorMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredEmissionFactorMilestones(store: TieredEmissionFactorMilestoneStore, query: TieredEmissionFactorMilestoneQuery): MutableList<TieredEmissionFactorMilestone> {
    return query.apply(store.list())
}
