package com.acme.transport.terminal.dwell.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell reviews by code. */
interface DwellReviewStore {
    fun find(code: String): DwellReview?
    fun save(item: DwellReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellReviewStore(
    var items: MutableMap<String, DwellReview>,
    var order: MutableList<String>,
) : DwellReviewStore {
    override fun find(code: String): DwellReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellReview) {
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

    override fun list(): MutableList<DwellReview> {
        val out = mutableListOf<DwellReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellReviewStore(): DwellReviewStore {
    return MemoryDwellReviewStore(mutableMapOf<String, DwellReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellReviewStore(
    var inner: DwellReviewStore,
    var cache: TtlCache<DwellReview>,
    var hits: Int,
    var misses: Int,
) : DwellReviewStore {
    override fun find(code: String): DwellReview? {
        val hit = (cache.get(code) as DwellReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellReview> = inner.list()
}

fun newCachedDwellReviewStore(inner: DwellReviewStore, ttl: Int, clock: Clock): DwellReviewStore {
    return CachedDwellReviewStore(
        inner = inner,
        cache = newTtlCache<DwellReview>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell reviews. Empty fields match all. */
data class DwellReviewQuery(
    var title: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellReview): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<DwellReview>): MutableList<DwellReview> {
        val matched = filter(items) { candidate: DwellReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellReviews(store: DwellReviewStore, query: DwellReviewQuery): MutableList<DwellReview> {
    return query.apply(store.list())
}
