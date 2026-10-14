package com.acme.transport.customs.declaration.summary

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration summaries by code. */
interface DeclarationSummaryStore {
    fun find(code: String): DeclarationSummary?
    fun save(item: DeclarationSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationSummaryStore(
    var items: MutableMap<String, DeclarationSummary>,
    var order: MutableList<String>,
) : DeclarationSummaryStore {
    override fun find(code: String): DeclarationSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationSummary) {
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

    override fun list(): MutableList<DeclarationSummary> {
        val out = mutableListOf<DeclarationSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationSummaryStore(): DeclarationSummaryStore {
    return MemoryDeclarationSummaryStore(mutableMapOf<String, DeclarationSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationSummaryStore(
    var inner: DeclarationSummaryStore,
    var cache: TtlCache<DeclarationSummary>,
    var hits: Int,
    var misses: Int,
) : DeclarationSummaryStore {
    override fun find(code: String): DeclarationSummary? {
        val hit = (cache.get(code) as DeclarationSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationSummary> = inner.list()
}

fun newCachedDeclarationSummaryStore(inner: DeclarationSummaryStore, ttl: Int, clock: Clock): DeclarationSummaryStore {
    return CachedDeclarationSummaryStore(
        inner = inner,
        cache = newTtlCache<DeclarationSummary>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration summaries. Empty fields match all. */
data class DeclarationSummaryQuery(
    var name: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationSummary): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationSummary>): MutableList<DeclarationSummary> {
        val matched = filter(items) { candidate: DeclarationSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationSummaries(store: DeclarationSummaryStore, query: DeclarationSummaryQuery): MutableList<DeclarationSummary> {
    return query.apply(store.list())
}
