package com.acme.transport.terminal.sortation.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps global sortation bundles by code. */
interface GlobalSortationBundleStore {
    fun find(code: String): GlobalSortationBundle?
    fun save(item: GlobalSortationBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalSortationBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalSortationBundleStore(
    var items: MutableMap<String, GlobalSortationBundle>,
    var order: MutableList<String>,
) : GlobalSortationBundleStore {
    override fun find(code: String): GlobalSortationBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalSortationBundle) {
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

    override fun list(): MutableList<GlobalSortationBundle> {
        val out = mutableListOf<GlobalSortationBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalSortationBundleStore(): GlobalSortationBundleStore {
    return MemoryGlobalSortationBundleStore(
        mutableMapOf<String, GlobalSortationBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalSortationBundleStore(
    var inner: GlobalSortationBundleStore,
    var cache: TtlCache<GlobalSortationBundle>,
    var hits: Int,
    var misses: Int,
) : GlobalSortationBundleStore {
    override fun find(code: String): GlobalSortationBundle? {
        val hit = (cache.get(code) as GlobalSortationBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalSortationBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalSortationBundle> = inner.list()
}

fun newCachedGlobalSortationBundleStore(inner: GlobalSortationBundleStore, ttl: Int, clock: Clock): GlobalSortationBundleStore {
    return CachedGlobalSortationBundleStore(
        inner = inner,
        cache = newTtlCache<GlobalSortationBundle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global sortation bundles. Empty fields match all. */
data class GlobalSortationBundleQuery(
    var title: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalSortationBundle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalSortationBundle>): MutableList<GlobalSortationBundle> {
        val matched = filter(items) { candidate: GlobalSortationBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalSortationBundles(store: GlobalSortationBundleStore, query: GlobalSortationBundleQuery): MutableList<GlobalSortationBundle> {
    return query.apply(store.list())
}
