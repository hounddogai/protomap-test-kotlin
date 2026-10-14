package com.acme.transport.network.servicearea.areamanifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area milestones by code. */
interface ServiceAreaMilestoneStore {
    fun find(code: String): ServiceAreaMilestone?
    fun save(item: ServiceAreaMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaMilestoneStore(
    var items: MutableMap<String, ServiceAreaMilestone>,
    var order: MutableList<String>,
) : ServiceAreaMilestoneStore {
    override fun find(code: String): ServiceAreaMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaMilestone) {
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

    override fun list(): MutableList<ServiceAreaMilestone> {
        val out = mutableListOf<ServiceAreaMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaMilestoneStore(): ServiceAreaMilestoneStore {
    return MemoryServiceAreaMilestoneStore(
        mutableMapOf<String, ServiceAreaMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaMilestoneStore(
    var inner: ServiceAreaMilestoneStore,
    var cache: TtlCache<ServiceAreaMilestone>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaMilestoneStore {
    override fun find(code: String): ServiceAreaMilestone? {
        val hit = (cache.get(code) as ServiceAreaMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaMilestone> = inner.list()
}

fun newCachedServiceAreaMilestoneStore(inner: ServiceAreaMilestoneStore, ttl: Int, clock: Clock): ServiceAreaMilestoneStore {
    return CachedServiceAreaMilestoneStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaMilestone>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area milestones. Empty fields match all. */
data class ServiceAreaMilestoneQuery(
    var title: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaMilestone>): MutableList<ServiceAreaMilestone> {
        val matched = filter(items) { candidate: ServiceAreaMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaMilestones(store: ServiceAreaMilestoneStore, query: ServiceAreaMilestoneQuery): MutableList<ServiceAreaMilestone> {
    return query.apply(store.list())
}
