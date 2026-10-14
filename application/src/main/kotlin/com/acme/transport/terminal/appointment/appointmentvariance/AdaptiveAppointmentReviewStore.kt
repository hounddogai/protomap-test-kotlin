package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps adaptive appointment reviews by code. */
interface AdaptiveAppointmentReviewStore {
    fun find(code: String): AdaptiveAppointmentReview?
    fun save(item: AdaptiveAppointmentReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveAppointmentReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveAppointmentReviewStore(
    var items: MutableMap<String, AdaptiveAppointmentReview>,
    var order: MutableList<String>,
) : AdaptiveAppointmentReviewStore {
    override fun find(code: String): AdaptiveAppointmentReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveAppointmentReview) {
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

    override fun list(): MutableList<AdaptiveAppointmentReview> {
        val out = mutableListOf<AdaptiveAppointmentReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveAppointmentReviewStore(): AdaptiveAppointmentReviewStore {
    return MemoryAdaptiveAppointmentReviewStore(
        mutableMapOf<String, AdaptiveAppointmentReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveAppointmentReviewStore(
    var inner: AdaptiveAppointmentReviewStore,
    var cache: TtlCache<AdaptiveAppointmentReview>,
    var hits: Int,
    var misses: Int,
) : AdaptiveAppointmentReviewStore {
    override fun find(code: String): AdaptiveAppointmentReview? {
        val hit = (cache.get(code) as AdaptiveAppointmentReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveAppointmentReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveAppointmentReview> = inner.list()
}

fun newCachedAdaptiveAppointmentReviewStore(inner: AdaptiveAppointmentReviewStore, ttl: Int, clock: Clock): AdaptiveAppointmentReviewStore {
    return CachedAdaptiveAppointmentReviewStore(
        inner = inner,
        cache = newTtlCache<AdaptiveAppointmentReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive appointment reviews. Empty fields match all. */
data class AdaptiveAppointmentReviewQuery(
    var title: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveAppointmentReview): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveAppointmentReview>): MutableList<AdaptiveAppointmentReview> {
        val matched = filter(items) { candidate: AdaptiveAppointmentReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveAppointmentReviews(store: AdaptiveAppointmentReviewStore, query: AdaptiveAppointmentReviewQuery): MutableList<AdaptiveAppointmentReview> {
    return query.apply(store.list())
}
