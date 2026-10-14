package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps fallback trailer yard bundles by code. */
interface FallbackTrailerYardBundleStore {
    fun find(code: String): FallbackTrailerYardBundle?
    fun save(item: FallbackTrailerYardBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackTrailerYardBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackTrailerYardBundleStore(
    var items: MutableMap<String, FallbackTrailerYardBundle>,
    var order: MutableList<String>,
) : FallbackTrailerYardBundleStore {
    override fun find(code: String): FallbackTrailerYardBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackTrailerYardBundle) {
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

    override fun list(): MutableList<FallbackTrailerYardBundle> {
        val out = mutableListOf<FallbackTrailerYardBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackTrailerYardBundleStore(): FallbackTrailerYardBundleStore {
    return MemoryFallbackTrailerYardBundleStore(
        mutableMapOf<String, FallbackTrailerYardBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackTrailerYardBundleStore(
    var inner: FallbackTrailerYardBundleStore,
    var cache: TtlCache<FallbackTrailerYardBundle>,
    var hits: Int,
    var misses: Int,
) : FallbackTrailerYardBundleStore {
    override fun find(code: String): FallbackTrailerYardBundle? {
        val hit = (cache.get(code) as FallbackTrailerYardBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackTrailerYardBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackTrailerYardBundle> = inner.list()
}

fun newCachedFallbackTrailerYardBundleStore(inner: FallbackTrailerYardBundleStore, ttl: Int, clock: Clock): FallbackTrailerYardBundleStore {
    return CachedFallbackTrailerYardBundleStore(
        inner = inner,
        cache = newTtlCache<FallbackTrailerYardBundle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback trailer yard bundles. Empty fields match all. */
data class FallbackTrailerYardBundleQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackTrailerYardBundle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackTrailerYardBundle>): MutableList<FallbackTrailerYardBundle> {
        val matched = filter(items) { candidate: FallbackTrailerYardBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackTrailerYardBundles(store: FallbackTrailerYardBundleStore, query: FallbackTrailerYardBundleQuery): MutableList<FallbackTrailerYardBundle> {
    return query.apply(store.list())
}
