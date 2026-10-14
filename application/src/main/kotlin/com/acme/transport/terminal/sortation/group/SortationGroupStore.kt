package com.acme.transport.terminal.sortation.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation groups by code. */
interface SortationGroupStore {
    fun find(code: String): SortationGroup?
    fun save(item: SortationGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationGroupStore(
    var items: MutableMap<String, SortationGroup>,
    var order: MutableList<String>,
) : SortationGroupStore {
    override fun find(code: String): SortationGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationGroup) {
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

    override fun list(): MutableList<SortationGroup> {
        val out = mutableListOf<SortationGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationGroupStore(): SortationGroupStore {
    return MemorySortationGroupStore(mutableMapOf<String, SortationGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationGroupStore(
    var inner: SortationGroupStore,
    var cache: TtlCache<SortationGroup>,
    var hits: Int,
    var misses: Int,
) : SortationGroupStore {
    override fun find(code: String): SortationGroup? {
        val hit = (cache.get(code) as SortationGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationGroup> = inner.list()
}

fun newCachedSortationGroupStore(inner: SortationGroupStore, ttl: Int, clock: Clock): SortationGroupStore {
    return CachedSortationGroupStore(
        inner = inner,
        cache = newTtlCache<SortationGroup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation groups. Empty fields match all. */
data class SortationGroupQuery(
    var unit: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationGroup): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SortationGroup>): MutableList<SortationGroup> {
        val matched = filter(items) { candidate: SortationGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationGroups(store: SortationGroupStore, query: SortationGroupQuery): MutableList<SortationGroup> {
    return query.apply(store.list())
}
