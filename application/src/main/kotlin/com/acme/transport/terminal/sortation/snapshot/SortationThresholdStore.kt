package com.acme.transport.terminal.sortation.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation thresholds by code. */
interface SortationThresholdStore {
    fun find(code: String): SortationThreshold?
    fun save(item: SortationThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationThresholdStore(
    var items: MutableMap<String, SortationThreshold>,
    var order: MutableList<String>,
) : SortationThresholdStore {
    override fun find(code: String): SortationThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationThreshold) {
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

    override fun list(): MutableList<SortationThreshold> {
        val out = mutableListOf<SortationThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationThresholdStore(): SortationThresholdStore {
    return MemorySortationThresholdStore(mutableMapOf<String, SortationThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationThresholdStore(
    var inner: SortationThresholdStore,
    var cache: TtlCache<SortationThreshold>,
    var hits: Int,
    var misses: Int,
) : SortationThresholdStore {
    override fun find(code: String): SortationThreshold? {
        val hit = (cache.get(code) as SortationThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationThreshold> = inner.list()
}

fun newCachedSortationThresholdStore(inner: SortationThresholdStore, ttl: Int, clock: Clock): SortationThresholdStore {
    return CachedSortationThresholdStore(
        inner = inner,
        cache = newTtlCache<SortationThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation thresholds. Empty fields match all. */
data class SortationThresholdQuery(
    var label: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationThreshold): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<SortationThreshold>): MutableList<SortationThreshold> {
        val matched = filter(items) { candidate: SortationThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationThresholds(store: SortationThresholdStore, query: SortationThresholdQuery): MutableList<SortationThreshold> {
    return query.apply(store.list())
}
