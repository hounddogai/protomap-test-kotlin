package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps weighted lane plan bundles by code. */
interface WeightedLanePlanBundleStore {
    fun find(code: String): WeightedLanePlanBundle?
    fun save(item: WeightedLanePlanBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedLanePlanBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedLanePlanBundleStore(
    var items: MutableMap<String, WeightedLanePlanBundle>,
    var order: MutableList<String>,
) : WeightedLanePlanBundleStore {
    override fun find(code: String): WeightedLanePlanBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedLanePlanBundle) {
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

    override fun list(): MutableList<WeightedLanePlanBundle> {
        val out = mutableListOf<WeightedLanePlanBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedLanePlanBundleStore(): WeightedLanePlanBundleStore {
    return MemoryWeightedLanePlanBundleStore(
        mutableMapOf<String, WeightedLanePlanBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedLanePlanBundleStore(
    var inner: WeightedLanePlanBundleStore,
    var cache: TtlCache<WeightedLanePlanBundle>,
    var hits: Int,
    var misses: Int,
) : WeightedLanePlanBundleStore {
    override fun find(code: String): WeightedLanePlanBundle? {
        val hit = (cache.get(code) as WeightedLanePlanBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedLanePlanBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedLanePlanBundle> = inner.list()
}

fun newCachedWeightedLanePlanBundleStore(inner: WeightedLanePlanBundleStore, ttl: Int, clock: Clock): WeightedLanePlanBundleStore {
    return CachedWeightedLanePlanBundleStore(
        inner = inner,
        cache = newTtlCache<WeightedLanePlanBundle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted lane plan bundles. Empty fields match all. */
data class WeightedLanePlanBundleQuery(
    var label: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedLanePlanBundle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedLanePlanBundle>): MutableList<WeightedLanePlanBundle> {
        val matched = filter(items) { candidate: WeightedLanePlanBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedLanePlanBundles(store: WeightedLanePlanBundleStore, query: WeightedLanePlanBundleQuery): MutableList<WeightedLanePlanBundle> {
    return query.apply(store.list())
}
