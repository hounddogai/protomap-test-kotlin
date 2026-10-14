package com.acme.transport.linehaul.relay.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lenient relay summaries by code. */
interface LenientRelaySummaryStore {
    fun find(code: String): LenientRelaySummary?
    fun save(item: LenientRelaySummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientRelaySummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientRelaySummaryStore(
    var items: MutableMap<String, LenientRelaySummary>,
    var order: MutableList<String>,
) : LenientRelaySummaryStore {
    override fun find(code: String): LenientRelaySummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientRelaySummary) {
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

    override fun list(): MutableList<LenientRelaySummary> {
        val out = mutableListOf<LenientRelaySummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientRelaySummaryStore(): LenientRelaySummaryStore {
    return MemoryLenientRelaySummaryStore(mutableMapOf<String, LenientRelaySummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientRelaySummaryStore(
    var inner: LenientRelaySummaryStore,
    var cache: TtlCache<LenientRelaySummary>,
    var hits: Int,
    var misses: Int,
) : LenientRelaySummaryStore {
    override fun find(code: String): LenientRelaySummary? {
        val hit = (cache.get(code) as LenientRelaySummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientRelaySummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientRelaySummary> = inner.list()
}

fun newCachedLenientRelaySummaryStore(inner: LenientRelaySummaryStore, ttl: Int, clock: Clock): LenientRelaySummaryStore {
    return CachedLenientRelaySummaryStore(
        inner = inner,
        cache = newTtlCache<LenientRelaySummary>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient relay summaries. Empty fields match all. */
data class LenientRelaySummaryQuery(
    var title: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientRelaySummary): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientRelaySummary>): MutableList<LenientRelaySummary> {
        val matched = filter(items) { candidate: LenientRelaySummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientRelaySummaries(store: LenientRelaySummaryStore, query: LenientRelaySummaryQuery): MutableList<LenientRelaySummary> {
    return query.apply(store.list())
}
