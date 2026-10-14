package com.acme.transport.customs.inspection.notice

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy inspection reviews by code. */
interface LegacyInspectionReviewStore {
    fun find(code: String): LegacyInspectionReview?
    fun save(item: LegacyInspectionReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyInspectionReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyInspectionReviewStore(
    var items: MutableMap<String, LegacyInspectionReview>,
    var order: MutableList<String>,
) : LegacyInspectionReviewStore {
    override fun find(code: String): LegacyInspectionReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyInspectionReview) {
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

    override fun list(): MutableList<LegacyInspectionReview> {
        val out = mutableListOf<LegacyInspectionReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyInspectionReviewStore(): LegacyInspectionReviewStore {
    return MemoryLegacyInspectionReviewStore(
        mutableMapOf<String, LegacyInspectionReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyInspectionReviewStore(
    var inner: LegacyInspectionReviewStore,
    var cache: TtlCache<LegacyInspectionReview>,
    var hits: Int,
    var misses: Int,
) : LegacyInspectionReviewStore {
    override fun find(code: String): LegacyInspectionReview? {
        val hit = (cache.get(code) as LegacyInspectionReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyInspectionReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyInspectionReview> = inner.list()
}

fun newCachedLegacyInspectionReviewStore(inner: LegacyInspectionReviewStore, ttl: Int, clock: Clock): LegacyInspectionReviewStore {
    return CachedLegacyInspectionReviewStore(
        inner = inner,
        cache = newTtlCache<LegacyInspectionReview>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy inspection reviews. Empty fields match all. */
data class LegacyInspectionReviewQuery(
    var lane: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyInspectionReview): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<LegacyInspectionReview>): MutableList<LegacyInspectionReview> {
        val matched = filter(items) { candidate: LegacyInspectionReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyInspectionReviews(store: LegacyInspectionReviewStore, query: LegacyInspectionReviewQuery): MutableList<LegacyInspectionReview> {
    return query.apply(store.list())
}
