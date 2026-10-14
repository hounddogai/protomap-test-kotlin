package com.acme.transport.lastmile.drop.dropbundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps weighted drop bundles by code. */
interface WeightedDropBundleStore {
    fun find(code: String): WeightedDropBundle?
    fun save(item: WeightedDropBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDropBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDropBundleStore(
    var items: MutableMap<String, WeightedDropBundle>,
    var order: MutableList<String>,
) : WeightedDropBundleStore {
    override fun find(code: String): WeightedDropBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDropBundle) {
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

    override fun list(): MutableList<WeightedDropBundle> {
        val out = mutableListOf<WeightedDropBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDropBundleStore(): WeightedDropBundleStore {
    return MemoryWeightedDropBundleStore(mutableMapOf<String, WeightedDropBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDropBundleStore(
    var inner: WeightedDropBundleStore,
    var cache: TtlCache<WeightedDropBundle>,
    var hits: Int,
    var misses: Int,
) : WeightedDropBundleStore {
    override fun find(code: String): WeightedDropBundle? {
        val hit = (cache.get(code) as WeightedDropBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDropBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDropBundle> = inner.list()
}

fun newCachedWeightedDropBundleStore(inner: WeightedDropBundleStore, ttl: Int, clock: Clock): WeightedDropBundleStore {
    return CachedWeightedDropBundleStore(
        inner = inner,
        cache = newTtlCache<WeightedDropBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted drop bundles. Empty fields match all. */
data class WeightedDropBundleQuery(
    var unit: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDropBundle): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedDropBundle>): MutableList<WeightedDropBundle> {
        val matched = filter(items) { candidate: WeightedDropBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDropBundles(store: WeightedDropBundleStore, query: WeightedDropBundleQuery): MutableList<WeightedDropBundle> {
    return query.apply(store.list())
}
