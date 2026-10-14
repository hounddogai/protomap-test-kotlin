package com.acme.transport.terminal.dwell.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps fallback dwell bundles by code. */
interface FallbackDwellBundleStore {
    fun find(code: String): FallbackDwellBundle?
    fun save(item: FallbackDwellBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDwellBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDwellBundleStore(
    var items: MutableMap<String, FallbackDwellBundle>,
    var order: MutableList<String>,
) : FallbackDwellBundleStore {
    override fun find(code: String): FallbackDwellBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDwellBundle) {
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

    override fun list(): MutableList<FallbackDwellBundle> {
        val out = mutableListOf<FallbackDwellBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDwellBundleStore(): FallbackDwellBundleStore {
    return MemoryFallbackDwellBundleStore(mutableMapOf<String, FallbackDwellBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDwellBundleStore(
    var inner: FallbackDwellBundleStore,
    var cache: TtlCache<FallbackDwellBundle>,
    var hits: Int,
    var misses: Int,
) : FallbackDwellBundleStore {
    override fun find(code: String): FallbackDwellBundle? {
        val hit = (cache.get(code) as FallbackDwellBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDwellBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDwellBundle> = inner.list()
}

fun newCachedFallbackDwellBundleStore(inner: FallbackDwellBundleStore, ttl: Int, clock: Clock): FallbackDwellBundleStore {
    return CachedFallbackDwellBundleStore(
        inner = inner,
        cache = newTtlCache<FallbackDwellBundle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback dwell bundles. Empty fields match all. */
data class FallbackDwellBundleQuery(
    var segment: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDwellBundle): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDwellBundle>): MutableList<FallbackDwellBundle> {
        val matched = filter(items) { candidate: FallbackDwellBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDwellBundles(store: FallbackDwellBundleStore, query: FallbackDwellBundleQuery): MutableList<FallbackDwellBundle> {
    return query.apply(store.list())
}
