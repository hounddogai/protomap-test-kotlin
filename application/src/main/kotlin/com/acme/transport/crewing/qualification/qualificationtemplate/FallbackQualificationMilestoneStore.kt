package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback qualification milestones by code. */
interface FallbackQualificationMilestoneStore {
    fun find(code: String): FallbackQualificationMilestone?
    fun save(item: FallbackQualificationMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackQualificationMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackQualificationMilestoneStore(
    var items: MutableMap<String, FallbackQualificationMilestone>,
    var order: MutableList<String>,
) : FallbackQualificationMilestoneStore {
    override fun find(code: String): FallbackQualificationMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackQualificationMilestone) {
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

    override fun list(): MutableList<FallbackQualificationMilestone> {
        val out = mutableListOf<FallbackQualificationMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackQualificationMilestoneStore(): FallbackQualificationMilestoneStore {
    return MemoryFallbackQualificationMilestoneStore(
        mutableMapOf<String, FallbackQualificationMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackQualificationMilestoneStore(
    var inner: FallbackQualificationMilestoneStore,
    var cache: TtlCache<FallbackQualificationMilestone>,
    var hits: Int,
    var misses: Int,
) : FallbackQualificationMilestoneStore {
    override fun find(code: String): FallbackQualificationMilestone? {
        val hit = (cache.get(code) as FallbackQualificationMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackQualificationMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackQualificationMilestone> = inner.list()
}

fun newCachedFallbackQualificationMilestoneStore(inner: FallbackQualificationMilestoneStore, ttl: Int, clock: Clock): FallbackQualificationMilestoneStore {
    return CachedFallbackQualificationMilestoneStore(
        inner = inner,
        cache = newTtlCache<FallbackQualificationMilestone>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback qualification milestones. Empty fields match all. */
data class FallbackQualificationMilestoneQuery(
    var channel: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackQualificationMilestone): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<FallbackQualificationMilestone>): MutableList<FallbackQualificationMilestone> {
        val matched = filter(items) { candidate: FallbackQualificationMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackQualificationMilestones(store: FallbackQualificationMilestoneStore, query: FallbackQualificationMilestoneQuery): MutableList<FallbackQualificationMilestone> {
    return query.apply(store.list())
}
