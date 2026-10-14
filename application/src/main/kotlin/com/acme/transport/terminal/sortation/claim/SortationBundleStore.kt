package com.acme.transport.terminal.sortation.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation bundles by code. */
interface SortationBundleStore {
    fun find(code: String): SortationBundle?
    fun save(item: SortationBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationBundleStore(
    var items: MutableMap<String, SortationBundle>,
    var order: MutableList<String>,
) : SortationBundleStore {
    override fun find(code: String): SortationBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationBundle) {
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

    override fun list(): MutableList<SortationBundle> {
        val out = mutableListOf<SortationBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationBundleStore(): SortationBundleStore {
    return MemorySortationBundleStore(mutableMapOf<String, SortationBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationBundleStore(
    var inner: SortationBundleStore,
    var cache: TtlCache<SortationBundle>,
    var hits: Int,
    var misses: Int,
) : SortationBundleStore {
    override fun find(code: String): SortationBundle? {
        val hit = (cache.get(code) as SortationBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationBundle> = inner.list()
}

fun newCachedSortationBundleStore(inner: SortationBundleStore, ttl: Int, clock: Clock): SortationBundleStore {
    return CachedSortationBundleStore(
        inner = inner,
        cache = newTtlCache<SortationBundle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation bundles. Empty fields match all. */
data class SortationBundleQuery(
    var title: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationBundle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SortationBundle>): MutableList<SortationBundle> {
        val matched = filter(items) { candidate: SortationBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationBundles(store: SortationBundleStore, query: SortationBundleQuery): MutableList<SortationBundle> {
    return query.apply(store.list())
}
