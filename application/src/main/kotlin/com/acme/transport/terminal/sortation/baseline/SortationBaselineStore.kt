package com.acme.transport.terminal.sortation.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation baselines by code. */
interface SortationBaselineStore {
    fun find(code: String): SortationBaseline?
    fun save(item: SortationBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationBaselineStore(
    var items: MutableMap<String, SortationBaseline>,
    var order: MutableList<String>,
) : SortationBaselineStore {
    override fun find(code: String): SortationBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationBaseline) {
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

    override fun list(): MutableList<SortationBaseline> {
        val out = mutableListOf<SortationBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationBaselineStore(): SortationBaselineStore {
    return MemorySortationBaselineStore(mutableMapOf<String, SortationBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationBaselineStore(
    var inner: SortationBaselineStore,
    var cache: TtlCache<SortationBaseline>,
    var hits: Int,
    var misses: Int,
) : SortationBaselineStore {
    override fun find(code: String): SortationBaseline? {
        val hit = (cache.get(code) as SortationBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationBaseline> = inner.list()
}

fun newCachedSortationBaselineStore(inner: SortationBaselineStore, ttl: Int, clock: Clock): SortationBaselineStore {
    return CachedSortationBaselineStore(
        inner = inner,
        cache = newTtlCache<SortationBaseline>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation baselines. Empty fields match all. */
data class SortationBaselineQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationBaseline): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SortationBaseline>): MutableList<SortationBaseline> {
        val matched = filter(items) { candidate: SortationBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationBaselines(store: SortationBaselineStore, query: SortationBaselineQuery): MutableList<SortationBaseline> {
    return query.apply(store.list())
}
