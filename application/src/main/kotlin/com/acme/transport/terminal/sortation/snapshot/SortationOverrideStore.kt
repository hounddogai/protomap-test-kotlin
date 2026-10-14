package com.acme.transport.terminal.sortation.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation overrides by code. */
interface SortationOverrideStore {
    fun find(code: String): SortationOverride?
    fun save(item: SortationOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationOverrideStore(
    var items: MutableMap<String, SortationOverride>,
    var order: MutableList<String>,
) : SortationOverrideStore {
    override fun find(code: String): SortationOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationOverride) {
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

    override fun list(): MutableList<SortationOverride> {
        val out = mutableListOf<SortationOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationOverrideStore(): SortationOverrideStore {
    return MemorySortationOverrideStore(mutableMapOf<String, SortationOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationOverrideStore(
    var inner: SortationOverrideStore,
    var cache: TtlCache<SortationOverride>,
    var hits: Int,
    var misses: Int,
) : SortationOverrideStore {
    override fun find(code: String): SortationOverride? {
        val hit = (cache.get(code) as SortationOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationOverride> = inner.list()
}

fun newCachedSortationOverrideStore(inner: SortationOverrideStore, ttl: Int, clock: Clock): SortationOverrideStore {
    return CachedSortationOverrideStore(
        inner = inner,
        cache = newTtlCache<SortationOverride>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation overrides. Empty fields match all. */
data class SortationOverrideQuery(
    var label: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationOverride): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<SortationOverride>): MutableList<SortationOverride> {
        val matched = filter(items) { candidate: SortationOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationOverrides(store: SortationOverrideStore, query: SortationOverrideQuery): MutableList<SortationOverride> {
    return query.apply(store.list())
}
