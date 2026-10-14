package com.acme.transport.terminal.sortation.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation variances by code. */
interface SortationVarianceStore {
    fun find(code: String): SortationVariance?
    fun save(item: SortationVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationVarianceStore(
    var items: MutableMap<String, SortationVariance>,
    var order: MutableList<String>,
) : SortationVarianceStore {
    override fun find(code: String): SortationVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationVariance) {
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

    override fun list(): MutableList<SortationVariance> {
        val out = mutableListOf<SortationVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationVarianceStore(): SortationVarianceStore {
    return MemorySortationVarianceStore(mutableMapOf<String, SortationVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationVarianceStore(
    var inner: SortationVarianceStore,
    var cache: TtlCache<SortationVariance>,
    var hits: Int,
    var misses: Int,
) : SortationVarianceStore {
    override fun find(code: String): SortationVariance? {
        val hit = (cache.get(code) as SortationVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationVariance> = inner.list()
}

fun newCachedSortationVarianceStore(inner: SortationVarianceStore, ttl: Int, clock: Clock): SortationVarianceStore {
    return CachedSortationVarianceStore(
        inner = inner,
        cache = newTtlCache<SortationVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation variances. Empty fields match all. */
data class SortationVarianceQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationVariance): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SortationVariance>): MutableList<SortationVariance> {
        val matched = filter(items) { candidate: SortationVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationVariances(store: SortationVarianceStore, query: SortationVarianceQuery): MutableList<SortationVariance> {
    return query.apply(store.list())
}
