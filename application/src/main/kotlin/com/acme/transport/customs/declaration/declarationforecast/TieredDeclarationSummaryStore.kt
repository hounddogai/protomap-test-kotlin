package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered declaration summaries by code. */
interface TieredDeclarationSummaryStore {
    fun find(code: String): TieredDeclarationSummary?
    fun save(item: TieredDeclarationSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDeclarationSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDeclarationSummaryStore(
    var items: MutableMap<String, TieredDeclarationSummary>,
    var order: MutableList<String>,
) : TieredDeclarationSummaryStore {
    override fun find(code: String): TieredDeclarationSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDeclarationSummary) {
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

    override fun list(): MutableList<TieredDeclarationSummary> {
        val out = mutableListOf<TieredDeclarationSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDeclarationSummaryStore(): TieredDeclarationSummaryStore {
    return MemoryTieredDeclarationSummaryStore(
        mutableMapOf<String, TieredDeclarationSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDeclarationSummaryStore(
    var inner: TieredDeclarationSummaryStore,
    var cache: TtlCache<TieredDeclarationSummary>,
    var hits: Int,
    var misses: Int,
) : TieredDeclarationSummaryStore {
    override fun find(code: String): TieredDeclarationSummary? {
        val hit = (cache.get(code) as TieredDeclarationSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDeclarationSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDeclarationSummary> = inner.list()
}

fun newCachedTieredDeclarationSummaryStore(inner: TieredDeclarationSummaryStore, ttl: Int, clock: Clock): TieredDeclarationSummaryStore {
    return CachedTieredDeclarationSummaryStore(
        inner = inner,
        cache = newTtlCache<TieredDeclarationSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered declaration summaries. Empty fields match all. */
data class TieredDeclarationSummaryQuery(
    var category: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDeclarationSummary): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<TieredDeclarationSummary>): MutableList<TieredDeclarationSummary> {
        val matched = filter(items) { candidate: TieredDeclarationSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDeclarationSummaries(store: TieredDeclarationSummaryStore, query: TieredDeclarationSummaryQuery): MutableList<TieredDeclarationSummary> {
    return query.apply(store.list())
}
