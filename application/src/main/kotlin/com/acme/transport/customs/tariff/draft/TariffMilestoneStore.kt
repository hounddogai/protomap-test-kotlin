package com.acme.transport.customs.tariff.draft

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff milestones by code. */
interface TariffMilestoneStore {
    fun find(code: String): TariffMilestone?
    fun save(item: TariffMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffMilestoneStore(
    var items: MutableMap<String, TariffMilestone>,
    var order: MutableList<String>,
) : TariffMilestoneStore {
    override fun find(code: String): TariffMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffMilestone) {
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

    override fun list(): MutableList<TariffMilestone> {
        val out = mutableListOf<TariffMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffMilestoneStore(): TariffMilestoneStore {
    return MemoryTariffMilestoneStore(mutableMapOf<String, TariffMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffMilestoneStore(
    var inner: TariffMilestoneStore,
    var cache: TtlCache<TariffMilestone>,
    var hits: Int,
    var misses: Int,
) : TariffMilestoneStore {
    override fun find(code: String): TariffMilestone? {
        val hit = (cache.get(code) as TariffMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffMilestone> = inner.list()
}

fun newCachedTariffMilestoneStore(inner: TariffMilestoneStore, ttl: Int, clock: Clock): TariffMilestoneStore {
    return CachedTariffMilestoneStore(
        inner = inner,
        cache = newTtlCache<TariffMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff milestones. Empty fields match all. */
data class TariffMilestoneQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffMilestone): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<TariffMilestone>): MutableList<TariffMilestone> {
        val matched = filter(items) { candidate: TariffMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffMilestones(store: TariffMilestoneStore, query: TariffMilestoneQuery): MutableList<TariffMilestone> {
    return query.apply(store.list())
}
