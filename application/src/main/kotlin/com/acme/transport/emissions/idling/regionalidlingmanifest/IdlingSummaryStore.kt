package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling summaries by code. */
interface IdlingSummaryStore {
    fun find(code: String): IdlingSummary?
    fun save(item: IdlingSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingSummaryStore(
    var items: MutableMap<String, IdlingSummary>,
    var order: MutableList<String>,
) : IdlingSummaryStore {
    override fun find(code: String): IdlingSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingSummary) {
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

    override fun list(): MutableList<IdlingSummary> {
        val out = mutableListOf<IdlingSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingSummaryStore(): IdlingSummaryStore {
    return MemoryIdlingSummaryStore(mutableMapOf<String, IdlingSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingSummaryStore(
    var inner: IdlingSummaryStore,
    var cache: TtlCache<IdlingSummary>,
    var hits: Int,
    var misses: Int,
) : IdlingSummaryStore {
    override fun find(code: String): IdlingSummary? {
        val hit = (cache.get(code) as IdlingSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingSummary> = inner.list()
}

fun newCachedIdlingSummaryStore(inner: IdlingSummaryStore, ttl: Int, clock: Clock): IdlingSummaryStore {
    return CachedIdlingSummaryStore(
        inner = inner,
        cache = newTtlCache<IdlingSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling summaries. Empty fields match all. */
data class IdlingSummaryQuery(
    var title: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingSummary): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingSummary>): MutableList<IdlingSummary> {
        val matched = filter(items) { candidate: IdlingSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingSummaries(store: IdlingSummaryStore, query: IdlingSummaryQuery): MutableList<IdlingSummary> {
    return query.apply(store.list())
}
