package com.acme.transport.lastmile.attempt.attemptmetric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt summaries by code. */
interface AttemptSummaryStore {
    fun find(code: String): AttemptSummary?
    fun save(item: AttemptSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptSummaryStore(
    var items: MutableMap<String, AttemptSummary>,
    var order: MutableList<String>,
) : AttemptSummaryStore {
    override fun find(code: String): AttemptSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptSummary) {
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

    override fun list(): MutableList<AttemptSummary> {
        val out = mutableListOf<AttemptSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptSummaryStore(): AttemptSummaryStore {
    return MemoryAttemptSummaryStore(mutableMapOf<String, AttemptSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptSummaryStore(
    var inner: AttemptSummaryStore,
    var cache: TtlCache<AttemptSummary>,
    var hits: Int,
    var misses: Int,
) : AttemptSummaryStore {
    override fun find(code: String): AttemptSummary? {
        val hit = (cache.get(code) as AttemptSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptSummary> = inner.list()
}

fun newCachedAttemptSummaryStore(inner: AttemptSummaryStore, ttl: Int, clock: Clock): AttemptSummaryStore {
    return CachedAttemptSummaryStore(
        inner = inner,
        cache = newTtlCache<AttemptSummary>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt summaries. Empty fields match all. */
data class AttemptSummaryQuery(
    var label: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptSummary): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptSummary>): MutableList<AttemptSummary> {
        val matched = filter(items) { candidate: AttemptSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptSummaries(store: AttemptSummaryStore, query: AttemptSummaryQuery): MutableList<AttemptSummary> {
    return query.apply(store.list())
}
