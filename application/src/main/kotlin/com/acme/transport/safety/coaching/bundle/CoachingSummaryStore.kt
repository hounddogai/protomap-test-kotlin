package com.acme.transport.safety.coaching.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching summaries by code. */
interface CoachingSummaryStore {
    fun find(code: String): CoachingSummary?
    fun save(item: CoachingSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingSummaryStore(
    var items: MutableMap<String, CoachingSummary>,
    var order: MutableList<String>,
) : CoachingSummaryStore {
    override fun find(code: String): CoachingSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingSummary) {
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

    override fun list(): MutableList<CoachingSummary> {
        val out = mutableListOf<CoachingSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingSummaryStore(): CoachingSummaryStore {
    return MemoryCoachingSummaryStore(mutableMapOf<String, CoachingSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingSummaryStore(
    var inner: CoachingSummaryStore,
    var cache: TtlCache<CoachingSummary>,
    var hits: Int,
    var misses: Int,
) : CoachingSummaryStore {
    override fun find(code: String): CoachingSummary? {
        val hit = (cache.get(code) as CoachingSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingSummary> = inner.list()
}

fun newCachedCoachingSummaryStore(inner: CoachingSummaryStore, ttl: Int, clock: Clock): CoachingSummaryStore {
    return CachedCoachingSummaryStore(
        inner = inner,
        cache = newTtlCache<CoachingSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching summaries. Empty fields match all. */
data class CoachingSummaryQuery(
    var depot: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingSummary): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingSummary>): MutableList<CoachingSummary> {
        val matched = filter(items) { candidate: CoachingSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingSummaries(store: CoachingSummaryStore, query: CoachingSummaryQuery): MutableList<CoachingSummary> {
    return query.apply(store.list())
}
