package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive emission factor milestones by code. */
interface AdaptiveEmissionFactorMilestoneStore {
    fun find(code: String): AdaptiveEmissionFactorMilestone?
    fun save(item: AdaptiveEmissionFactorMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveEmissionFactorMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveEmissionFactorMilestoneStore(
    var items: MutableMap<String, AdaptiveEmissionFactorMilestone>,
    var order: MutableList<String>,
) : AdaptiveEmissionFactorMilestoneStore {
    override fun find(code: String): AdaptiveEmissionFactorMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveEmissionFactorMilestone) {
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

    override fun list(): MutableList<AdaptiveEmissionFactorMilestone> {
        val out = mutableListOf<AdaptiveEmissionFactorMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveEmissionFactorMilestoneStore(): AdaptiveEmissionFactorMilestoneStore {
    return MemoryAdaptiveEmissionFactorMilestoneStore(
        mutableMapOf<String, AdaptiveEmissionFactorMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveEmissionFactorMilestoneStore(
    var inner: AdaptiveEmissionFactorMilestoneStore,
    var cache: TtlCache<AdaptiveEmissionFactorMilestone>,
    var hits: Int,
    var misses: Int,
) : AdaptiveEmissionFactorMilestoneStore {
    override fun find(code: String): AdaptiveEmissionFactorMilestone? {
        val hit = (cache.get(code) as AdaptiveEmissionFactorMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveEmissionFactorMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveEmissionFactorMilestone> = inner.list()
}

fun newCachedAdaptiveEmissionFactorMilestoneStore(inner: AdaptiveEmissionFactorMilestoneStore, ttl: Int, clock: Clock): AdaptiveEmissionFactorMilestoneStore {
    return CachedAdaptiveEmissionFactorMilestoneStore(
        inner = inner,
        cache = newTtlCache<AdaptiveEmissionFactorMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive emission factor milestones. Empty fields match all. */
data class AdaptiveEmissionFactorMilestoneQuery(
    var title: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveEmissionFactorMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveEmissionFactorMilestone>): MutableList<AdaptiveEmissionFactorMilestone> {
        val matched = filter(items) { candidate: AdaptiveEmissionFactorMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveEmissionFactorMilestones(store: AdaptiveEmissionFactorMilestoneStore, query: AdaptiveEmissionFactorMilestoneQuery): MutableList<AdaptiveEmissionFactorMilestone> {
    return query.apply(store.list())
}
