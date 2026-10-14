package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps manual lane plan bundles by code. */
interface ManualLanePlanBundleStore {
    fun find(code: String): ManualLanePlanBundle?
    fun save(item: ManualLanePlanBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualLanePlanBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualLanePlanBundleStore(
    var items: MutableMap<String, ManualLanePlanBundle>,
    var order: MutableList<String>,
) : ManualLanePlanBundleStore {
    override fun find(code: String): ManualLanePlanBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualLanePlanBundle) {
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

    override fun list(): MutableList<ManualLanePlanBundle> {
        val out = mutableListOf<ManualLanePlanBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualLanePlanBundleStore(): ManualLanePlanBundleStore {
    return MemoryManualLanePlanBundleStore(
        mutableMapOf<String, ManualLanePlanBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualLanePlanBundleStore(
    var inner: ManualLanePlanBundleStore,
    var cache: TtlCache<ManualLanePlanBundle>,
    var hits: Int,
    var misses: Int,
) : ManualLanePlanBundleStore {
    override fun find(code: String): ManualLanePlanBundle? {
        val hit = (cache.get(code) as ManualLanePlanBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualLanePlanBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualLanePlanBundle> = inner.list()
}

fun newCachedManualLanePlanBundleStore(inner: ManualLanePlanBundleStore, ttl: Int, clock: Clock): ManualLanePlanBundleStore {
    return CachedManualLanePlanBundleStore(
        inner = inner,
        cache = newTtlCache<ManualLanePlanBundle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual lane plan bundles. Empty fields match all. */
data class ManualLanePlanBundleQuery(
    var depot: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualLanePlanBundle): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualLanePlanBundle>): MutableList<ManualLanePlanBundle> {
        val matched = filter(items) { candidate: ManualLanePlanBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualLanePlanBundles(store: ManualLanePlanBundleStore, query: ManualLanePlanBundleQuery): MutableList<ManualLanePlanBundle> {
    return query.apply(store.list())
}
