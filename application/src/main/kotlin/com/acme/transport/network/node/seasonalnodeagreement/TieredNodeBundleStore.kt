package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps tiered node bundles by code. */
interface TieredNodeBundleStore {
    fun find(code: String): TieredNodeBundle?
    fun save(item: TieredNodeBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredNodeBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredNodeBundleStore(
    var items: MutableMap<String, TieredNodeBundle>,
    var order: MutableList<String>,
) : TieredNodeBundleStore {
    override fun find(code: String): TieredNodeBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredNodeBundle) {
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

    override fun list(): MutableList<TieredNodeBundle> {
        val out = mutableListOf<TieredNodeBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredNodeBundleStore(): TieredNodeBundleStore {
    return MemoryTieredNodeBundleStore(mutableMapOf<String, TieredNodeBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredNodeBundleStore(
    var inner: TieredNodeBundleStore,
    var cache: TtlCache<TieredNodeBundle>,
    var hits: Int,
    var misses: Int,
) : TieredNodeBundleStore {
    override fun find(code: String): TieredNodeBundle? {
        val hit = (cache.get(code) as TieredNodeBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredNodeBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredNodeBundle> = inner.list()
}

fun newCachedTieredNodeBundleStore(inner: TieredNodeBundleStore, ttl: Int, clock: Clock): TieredNodeBundleStore {
    return CachedTieredNodeBundleStore(
        inner = inner,
        cache = newTtlCache<TieredNodeBundle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered node bundles. Empty fields match all. */
data class TieredNodeBundleQuery(
    var lane: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredNodeBundle): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredNodeBundle>): MutableList<TieredNodeBundle> {
        val matched = filter(items) { candidate: TieredNodeBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredNodeBundles(store: TieredNodeBundleStore, query: TieredNodeBundleQuery): MutableList<TieredNodeBundle> {
    return query.apply(store.list())
}
