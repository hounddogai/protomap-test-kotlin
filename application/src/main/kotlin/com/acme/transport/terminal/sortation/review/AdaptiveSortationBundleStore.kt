package com.acme.transport.terminal.sortation.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps adaptive sortation bundles by code. */
interface AdaptiveSortationBundleStore {
    fun find(code: String): AdaptiveSortationBundle?
    fun save(item: AdaptiveSortationBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveSortationBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveSortationBundleStore(
    var items: MutableMap<String, AdaptiveSortationBundle>,
    var order: MutableList<String>,
) : AdaptiveSortationBundleStore {
    override fun find(code: String): AdaptiveSortationBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveSortationBundle) {
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

    override fun list(): MutableList<AdaptiveSortationBundle> {
        val out = mutableListOf<AdaptiveSortationBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveSortationBundleStore(): AdaptiveSortationBundleStore {
    return MemoryAdaptiveSortationBundleStore(
        mutableMapOf<String, AdaptiveSortationBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveSortationBundleStore(
    var inner: AdaptiveSortationBundleStore,
    var cache: TtlCache<AdaptiveSortationBundle>,
    var hits: Int,
    var misses: Int,
) : AdaptiveSortationBundleStore {
    override fun find(code: String): AdaptiveSortationBundle? {
        val hit = (cache.get(code) as AdaptiveSortationBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveSortationBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveSortationBundle> = inner.list()
}

fun newCachedAdaptiveSortationBundleStore(inner: AdaptiveSortationBundleStore, ttl: Int, clock: Clock): AdaptiveSortationBundleStore {
    return CachedAdaptiveSortationBundleStore(
        inner = inner,
        cache = newTtlCache<AdaptiveSortationBundle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive sortation bundles. Empty fields match all. */
data class AdaptiveSortationBundleQuery(
    var channel: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveSortationBundle): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveSortationBundle>): MutableList<AdaptiveSortationBundle> {
        val matched = filter(items) { candidate: AdaptiveSortationBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveSortationBundles(store: AdaptiveSortationBundleStore, query: AdaptiveSortationBundleQuery): MutableList<AdaptiveSortationBundle> {
    return query.apply(store.list())
}
