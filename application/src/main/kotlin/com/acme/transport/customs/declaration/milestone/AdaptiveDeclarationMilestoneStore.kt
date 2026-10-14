package com.acme.transport.customs.declaration.milestone

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive declaration milestones by code. */
interface AdaptiveDeclarationMilestoneStore {
    fun find(code: String): AdaptiveDeclarationMilestone?
    fun save(item: AdaptiveDeclarationMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDeclarationMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDeclarationMilestoneStore(
    var items: MutableMap<String, AdaptiveDeclarationMilestone>,
    var order: MutableList<String>,
) : AdaptiveDeclarationMilestoneStore {
    override fun find(code: String): AdaptiveDeclarationMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDeclarationMilestone) {
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

    override fun list(): MutableList<AdaptiveDeclarationMilestone> {
        val out = mutableListOf<AdaptiveDeclarationMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDeclarationMilestoneStore(): AdaptiveDeclarationMilestoneStore {
    return MemoryAdaptiveDeclarationMilestoneStore(
        mutableMapOf<String, AdaptiveDeclarationMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDeclarationMilestoneStore(
    var inner: AdaptiveDeclarationMilestoneStore,
    var cache: TtlCache<AdaptiveDeclarationMilestone>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDeclarationMilestoneStore {
    override fun find(code: String): AdaptiveDeclarationMilestone? {
        val hit = (cache.get(code) as AdaptiveDeclarationMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDeclarationMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDeclarationMilestone> = inner.list()
}

fun newCachedAdaptiveDeclarationMilestoneStore(inner: AdaptiveDeclarationMilestoneStore, ttl: Int, clock: Clock): AdaptiveDeclarationMilestoneStore {
    return CachedAdaptiveDeclarationMilestoneStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDeclarationMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive declaration milestones. Empty fields match all. */
data class AdaptiveDeclarationMilestoneQuery(
    var lane: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDeclarationMilestone): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveDeclarationMilestone>): MutableList<AdaptiveDeclarationMilestone> {
        val matched = filter(items) { candidate: AdaptiveDeclarationMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDeclarationMilestones(store: AdaptiveDeclarationMilestoneStore, query: AdaptiveDeclarationMilestoneQuery): MutableList<AdaptiveDeclarationMilestone> {
    return query.apply(store.list())
}
