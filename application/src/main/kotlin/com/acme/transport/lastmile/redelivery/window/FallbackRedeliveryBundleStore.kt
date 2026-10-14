package com.acme.transport.lastmile.redelivery.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps fallback redelivery bundles by code. */
interface FallbackRedeliveryBundleStore {
    fun find(code: String): FallbackRedeliveryBundle?
    fun save(item: FallbackRedeliveryBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRedeliveryBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRedeliveryBundleStore(
    var items: MutableMap<String, FallbackRedeliveryBundle>,
    var order: MutableList<String>,
) : FallbackRedeliveryBundleStore {
    override fun find(code: String): FallbackRedeliveryBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRedeliveryBundle) {
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

    override fun list(): MutableList<FallbackRedeliveryBundle> {
        val out = mutableListOf<FallbackRedeliveryBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRedeliveryBundleStore(): FallbackRedeliveryBundleStore {
    return MemoryFallbackRedeliveryBundleStore(
        mutableMapOf<String, FallbackRedeliveryBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRedeliveryBundleStore(
    var inner: FallbackRedeliveryBundleStore,
    var cache: TtlCache<FallbackRedeliveryBundle>,
    var hits: Int,
    var misses: Int,
) : FallbackRedeliveryBundleStore {
    override fun find(code: String): FallbackRedeliveryBundle? {
        val hit = (cache.get(code) as FallbackRedeliveryBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRedeliveryBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRedeliveryBundle> = inner.list()
}

fun newCachedFallbackRedeliveryBundleStore(inner: FallbackRedeliveryBundleStore, ttl: Int, clock: Clock): FallbackRedeliveryBundleStore {
    return CachedFallbackRedeliveryBundleStore(
        inner = inner,
        cache = newTtlCache<FallbackRedeliveryBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback redelivery bundles. Empty fields match all. */
data class FallbackRedeliveryBundleQuery(
    var locale: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRedeliveryBundle): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackRedeliveryBundle>): MutableList<FallbackRedeliveryBundle> {
        val matched = filter(items) { candidate: FallbackRedeliveryBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRedeliveryBundles(store: FallbackRedeliveryBundleStore, query: FallbackRedeliveryBundleQuery): MutableList<FallbackRedeliveryBundle> {
    return query.apply(store.list())
}
