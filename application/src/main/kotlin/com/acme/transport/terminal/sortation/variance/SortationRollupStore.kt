package com.acme.transport.terminal.sortation.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation rollups by code. */
interface SortationRollupStore {
    fun find(code: String): SortationRollup?
    fun save(item: SortationRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationRollupStore(
    var items: MutableMap<String, SortationRollup>,
    var order: MutableList<String>,
) : SortationRollupStore {
    override fun find(code: String): SortationRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationRollup) {
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

    override fun list(): MutableList<SortationRollup> {
        val out = mutableListOf<SortationRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationRollupStore(): SortationRollupStore {
    return MemorySortationRollupStore(mutableMapOf<String, SortationRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationRollupStore(
    var inner: SortationRollupStore,
    var cache: TtlCache<SortationRollup>,
    var hits: Int,
    var misses: Int,
) : SortationRollupStore {
    override fun find(code: String): SortationRollup? {
        val hit = (cache.get(code) as SortationRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationRollup> = inner.list()
}

fun newCachedSortationRollupStore(inner: SortationRollupStore, ttl: Int, clock: Clock): SortationRollupStore {
    return CachedSortationRollupStore(
        inner = inner,
        cache = newTtlCache<SortationRollup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation rollups. Empty fields match all. */
data class SortationRollupQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationRollup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SortationRollup>): MutableList<SortationRollup> {
        val matched = filter(items) { candidate: SortationRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationRollups(store: SortationRollupStore, query: SortationRollupQuery): MutableList<SortationRollup> {
    return query.apply(store.list())
}
