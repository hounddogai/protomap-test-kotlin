package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard bundles by code. */
interface TrailerYardBundleStore {
    fun find(code: String): TrailerYardBundle?
    fun save(item: TrailerYardBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardBundleStore(
    var items: MutableMap<String, TrailerYardBundle>,
    var order: MutableList<String>,
) : TrailerYardBundleStore {
    override fun find(code: String): TrailerYardBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardBundle) {
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

    override fun list(): MutableList<TrailerYardBundle> {
        val out = mutableListOf<TrailerYardBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardBundleStore(): TrailerYardBundleStore {
    return MemoryTrailerYardBundleStore(mutableMapOf<String, TrailerYardBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardBundleStore(
    var inner: TrailerYardBundleStore,
    var cache: TtlCache<TrailerYardBundle>,
    var hits: Int,
    var misses: Int,
) : TrailerYardBundleStore {
    override fun find(code: String): TrailerYardBundle? {
        val hit = (cache.get(code) as TrailerYardBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardBundle> = inner.list()
}

fun newCachedTrailerYardBundleStore(inner: TrailerYardBundleStore, ttl: Int, clock: Clock): TrailerYardBundleStore {
    return CachedTrailerYardBundleStore(
        inner = inner,
        cache = newTtlCache<TrailerYardBundle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard bundles. Empty fields match all. */
data class TrailerYardBundleQuery(
    var source: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardBundle): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<TrailerYardBundle>): MutableList<TrailerYardBundle> {
        val matched = filter(items) { candidate: TrailerYardBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardBundles(store: TrailerYardBundleStore, query: TrailerYardBundleQuery): MutableList<TrailerYardBundle> {
    return query.apply(store.list())
}
