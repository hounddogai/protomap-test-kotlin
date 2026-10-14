package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback lane plan bundles by code. */
interface FallbackLanePlanBundleStore {
    fun find(code: String): FallbackLanePlanBundle?
    fun save(item: FallbackLanePlanBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackLanePlanBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackLanePlanBundleStore(
    var items: MutableMap<String, FallbackLanePlanBundle>,
    var order: MutableList<String>,
) : FallbackLanePlanBundleStore {
    override fun find(code: String): FallbackLanePlanBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackLanePlanBundle) {
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

    override fun list(): MutableList<FallbackLanePlanBundle> {
        val out = mutableListOf<FallbackLanePlanBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackLanePlanBundleStore(): FallbackLanePlanBundleStore {
    return MemoryFallbackLanePlanBundleStore(
        mutableMapOf<String, FallbackLanePlanBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackLanePlanBundleStore(
    var inner: FallbackLanePlanBundleStore,
    var cache: TtlCache<FallbackLanePlanBundle>,
    var hits: Int,
    var misses: Int,
) : FallbackLanePlanBundleStore {
    override fun find(code: String): FallbackLanePlanBundle? {
        val hit = (cache.get(code) as FallbackLanePlanBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackLanePlanBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackLanePlanBundle> = inner.list()
}

fun newCachedFallbackLanePlanBundleStore(inner: FallbackLanePlanBundleStore, ttl: Int, clock: Clock): FallbackLanePlanBundleStore {
    return CachedFallbackLanePlanBundleStore(
        inner = inner,
        cache = newTtlCache<FallbackLanePlanBundle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback lane plan bundles. Empty fields match all. */
data class FallbackLanePlanBundleQuery(
    var locale: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackLanePlanBundle): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackLanePlanBundle>): MutableList<FallbackLanePlanBundle> {
        val matched = filter(items) { candidate: FallbackLanePlanBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackLanePlanBundles(store: FallbackLanePlanBundleStore, query: FallbackLanePlanBundleQuery): MutableList<FallbackLanePlanBundle> {
    return query.apply(store.list())
}
