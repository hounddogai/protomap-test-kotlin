package com.acme.transport.network.servicearea.primaryareatarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps capped service area variances by code. */
interface CappedServiceAreaVarianceStore {
    fun find(code: String): CappedServiceAreaVariance?
    fun save(item: CappedServiceAreaVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedServiceAreaVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedServiceAreaVarianceStore(
    var items: MutableMap<String, CappedServiceAreaVariance>,
    var order: MutableList<String>,
) : CappedServiceAreaVarianceStore {
    override fun find(code: String): CappedServiceAreaVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedServiceAreaVariance) {
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

    override fun list(): MutableList<CappedServiceAreaVariance> {
        val out = mutableListOf<CappedServiceAreaVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedServiceAreaVarianceStore(): CappedServiceAreaVarianceStore {
    return MemoryCappedServiceAreaVarianceStore(
        mutableMapOf<String, CappedServiceAreaVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedServiceAreaVarianceStore(
    var inner: CappedServiceAreaVarianceStore,
    var cache: TtlCache<CappedServiceAreaVariance>,
    var hits: Int,
    var misses: Int,
) : CappedServiceAreaVarianceStore {
    override fun find(code: String): CappedServiceAreaVariance? {
        val hit = (cache.get(code) as CappedServiceAreaVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedServiceAreaVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedServiceAreaVariance> = inner.list()
}

fun newCachedCappedServiceAreaVarianceStore(inner: CappedServiceAreaVarianceStore, ttl: Int, clock: Clock): CappedServiceAreaVarianceStore {
    return CachedCappedServiceAreaVarianceStore(
        inner = inner,
        cache = newTtlCache<CappedServiceAreaVariance>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped service area variances. Empty fields match all. */
data class CappedServiceAreaVarianceQuery(
    var source: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedServiceAreaVariance): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<CappedServiceAreaVariance>): MutableList<CappedServiceAreaVariance> {
        val matched = filter(items) { candidate: CappedServiceAreaVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedServiceAreaVariances(store: CappedServiceAreaVarianceStore, query: CappedServiceAreaVarianceQuery): MutableList<CappedServiceAreaVariance> {
    return query.apply(store.list())
}
