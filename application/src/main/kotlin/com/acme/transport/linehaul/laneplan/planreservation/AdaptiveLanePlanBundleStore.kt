package com.acme.transport.linehaul.laneplan.planreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive lane plan bundles by code. */
interface AdaptiveLanePlanBundleStore {
    fun find(code: String): AdaptiveLanePlanBundle?
    fun save(item: AdaptiveLanePlanBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveLanePlanBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveLanePlanBundleStore(
    var items: MutableMap<String, AdaptiveLanePlanBundle>,
    var order: MutableList<String>,
) : AdaptiveLanePlanBundleStore {
    override fun find(code: String): AdaptiveLanePlanBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveLanePlanBundle) {
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

    override fun list(): MutableList<AdaptiveLanePlanBundle> {
        val out = mutableListOf<AdaptiveLanePlanBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveLanePlanBundleStore(): AdaptiveLanePlanBundleStore {
    return MemoryAdaptiveLanePlanBundleStore(
        mutableMapOf<String, AdaptiveLanePlanBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveLanePlanBundleStore(
    var inner: AdaptiveLanePlanBundleStore,
    var cache: TtlCache<AdaptiveLanePlanBundle>,
    var hits: Int,
    var misses: Int,
) : AdaptiveLanePlanBundleStore {
    override fun find(code: String): AdaptiveLanePlanBundle? {
        val hit = (cache.get(code) as AdaptiveLanePlanBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveLanePlanBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveLanePlanBundle> = inner.list()
}

fun newCachedAdaptiveLanePlanBundleStore(inner: AdaptiveLanePlanBundleStore, ttl: Int, clock: Clock): AdaptiveLanePlanBundleStore {
    return CachedAdaptiveLanePlanBundleStore(
        inner = inner,
        cache = newTtlCache<AdaptiveLanePlanBundle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive lane plan bundles. Empty fields match all. */
data class AdaptiveLanePlanBundleQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveLanePlanBundle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<AdaptiveLanePlanBundle>): MutableList<AdaptiveLanePlanBundle> {
        val matched = filter(items) { candidate: AdaptiveLanePlanBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveLanePlanBundles(store: AdaptiveLanePlanBundleStore, query: AdaptiveLanePlanBundleQuery): MutableList<AdaptiveLanePlanBundle> {
    return query.apply(store.list())
}
