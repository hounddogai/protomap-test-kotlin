package com.acme.transport.freightpay.accessorial.template

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback accessorial milestones by code. */
interface FallbackAccessorialMilestoneStore {
    fun find(code: String): FallbackAccessorialMilestone?
    fun save(item: FallbackAccessorialMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackAccessorialMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackAccessorialMilestoneStore(
    var items: MutableMap<String, FallbackAccessorialMilestone>,
    var order: MutableList<String>,
) : FallbackAccessorialMilestoneStore {
    override fun find(code: String): FallbackAccessorialMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackAccessorialMilestone) {
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

    override fun list(): MutableList<FallbackAccessorialMilestone> {
        val out = mutableListOf<FallbackAccessorialMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackAccessorialMilestoneStore(): FallbackAccessorialMilestoneStore {
    return MemoryFallbackAccessorialMilestoneStore(
        mutableMapOf<String, FallbackAccessorialMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackAccessorialMilestoneStore(
    var inner: FallbackAccessorialMilestoneStore,
    var cache: TtlCache<FallbackAccessorialMilestone>,
    var hits: Int,
    var misses: Int,
) : FallbackAccessorialMilestoneStore {
    override fun find(code: String): FallbackAccessorialMilestone? {
        val hit = (cache.get(code) as FallbackAccessorialMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackAccessorialMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackAccessorialMilestone> = inner.list()
}

fun newCachedFallbackAccessorialMilestoneStore(inner: FallbackAccessorialMilestoneStore, ttl: Int, clock: Clock): FallbackAccessorialMilestoneStore {
    return CachedFallbackAccessorialMilestoneStore(
        inner = inner,
        cache = newTtlCache<FallbackAccessorialMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback accessorial milestones. Empty fields match all. */
data class FallbackAccessorialMilestoneQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackAccessorialMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackAccessorialMilestone>): MutableList<FallbackAccessorialMilestone> {
        val matched = filter(items) { candidate: FallbackAccessorialMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackAccessorialMilestones(store: FallbackAccessorialMilestoneStore, query: FallbackAccessorialMilestoneQuery): MutableList<FallbackAccessorialMilestone> {
    return query.apply(store.list())
}
