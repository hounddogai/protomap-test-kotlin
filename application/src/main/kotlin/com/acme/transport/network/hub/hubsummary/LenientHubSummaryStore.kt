package com.acme.transport.network.hub.hubsummary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient hub summaries by code. */
interface LenientHubSummaryStore {
    fun find(code: String): LenientHubSummary?
    fun save(item: LenientHubSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientHubSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientHubSummaryStore(
    var items: MutableMap<String, LenientHubSummary>,
    var order: MutableList<String>,
) : LenientHubSummaryStore {
    override fun find(code: String): LenientHubSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientHubSummary) {
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

    override fun list(): MutableList<LenientHubSummary> {
        val out = mutableListOf<LenientHubSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientHubSummaryStore(): LenientHubSummaryStore {
    return MemoryLenientHubSummaryStore(mutableMapOf<String, LenientHubSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientHubSummaryStore(
    var inner: LenientHubSummaryStore,
    var cache: TtlCache<LenientHubSummary>,
    var hits: Int,
    var misses: Int,
) : LenientHubSummaryStore {
    override fun find(code: String): LenientHubSummary? {
        val hit = (cache.get(code) as LenientHubSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientHubSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientHubSummary> = inner.list()
}

fun newCachedLenientHubSummaryStore(inner: LenientHubSummaryStore, ttl: Int, clock: Clock): LenientHubSummaryStore {
    return CachedLenientHubSummaryStore(
        inner = inner,
        cache = newTtlCache<LenientHubSummary>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient hub summaries. Empty fields match all. */
data class LenientHubSummaryQuery(
    var title: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientHubSummary): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientHubSummary>): MutableList<LenientHubSummary> {
        val matched = filter(items) { candidate: LenientHubSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientHubSummaries(store: LenientHubSummaryStore, query: LenientHubSummaryQuery): MutableList<LenientHubSummary> {
    return query.apply(store.list())
}
