package com.acme.transport.network.servicearea.primaryareatarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps manual service area variances by code. */
interface ManualServiceAreaVarianceStore {
    fun find(code: String): ManualServiceAreaVariance?
    fun save(item: ManualServiceAreaVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualServiceAreaVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualServiceAreaVarianceStore(
    var items: MutableMap<String, ManualServiceAreaVariance>,
    var order: MutableList<String>,
) : ManualServiceAreaVarianceStore {
    override fun find(code: String): ManualServiceAreaVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualServiceAreaVariance) {
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

    override fun list(): MutableList<ManualServiceAreaVariance> {
        val out = mutableListOf<ManualServiceAreaVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualServiceAreaVarianceStore(): ManualServiceAreaVarianceStore {
    return MemoryManualServiceAreaVarianceStore(
        mutableMapOf<String, ManualServiceAreaVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualServiceAreaVarianceStore(
    var inner: ManualServiceAreaVarianceStore,
    var cache: TtlCache<ManualServiceAreaVariance>,
    var hits: Int,
    var misses: Int,
) : ManualServiceAreaVarianceStore {
    override fun find(code: String): ManualServiceAreaVariance? {
        val hit = (cache.get(code) as ManualServiceAreaVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualServiceAreaVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualServiceAreaVariance> = inner.list()
}

fun newCachedManualServiceAreaVarianceStore(inner: ManualServiceAreaVarianceStore, ttl: Int, clock: Clock): ManualServiceAreaVarianceStore {
    return CachedManualServiceAreaVarianceStore(
        inner = inner,
        cache = newTtlCache<ManualServiceAreaVariance>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual service area variances. Empty fields match all. */
data class ManualServiceAreaVarianceQuery(
    var name: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualServiceAreaVariance): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualServiceAreaVariance>): MutableList<ManualServiceAreaVariance> {
        val matched = filter(items) { candidate: ManualServiceAreaVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualServiceAreaVariances(store: ManualServiceAreaVarianceStore, query: ManualServiceAreaVarianceQuery): MutableList<ManualServiceAreaVariance> {
    return query.apply(store.list())
}
