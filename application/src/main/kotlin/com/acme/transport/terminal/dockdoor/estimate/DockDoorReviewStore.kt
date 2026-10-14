package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door reviews by code. */
interface DockDoorReviewStore {
    fun find(code: String): DockDoorReview?
    fun save(item: DockDoorReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorReviewStore(
    var items: MutableMap<String, DockDoorReview>,
    var order: MutableList<String>,
) : DockDoorReviewStore {
    override fun find(code: String): DockDoorReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorReview) {
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

    override fun list(): MutableList<DockDoorReview> {
        val out = mutableListOf<DockDoorReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorReviewStore(): DockDoorReviewStore {
    return MemoryDockDoorReviewStore(mutableMapOf<String, DockDoorReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorReviewStore(
    var inner: DockDoorReviewStore,
    var cache: TtlCache<DockDoorReview>,
    var hits: Int,
    var misses: Int,
) : DockDoorReviewStore {
    override fun find(code: String): DockDoorReview? {
        val hit = (cache.get(code) as DockDoorReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorReview> = inner.list()
}

fun newCachedDockDoorReviewStore(inner: DockDoorReviewStore, ttl: Int, clock: Clock): DockDoorReviewStore {
    return CachedDockDoorReviewStore(
        inner = inner,
        cache = newTtlCache<DockDoorReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door reviews. Empty fields match all. */
data class DockDoorReviewQuery(
    var region: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorReview): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorReview>): MutableList<DockDoorReview> {
        val matched = filter(items) { candidate: DockDoorReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorReviews(store: DockDoorReviewStore, query: DockDoorReviewQuery): MutableList<DockDoorReview> {
    return query.apply(store.list())
}
