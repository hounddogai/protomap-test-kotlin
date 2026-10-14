package com.acme.transport.linehaul.tender.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps manual tender variances by code. */
interface ManualTenderVarianceStore {
    fun find(code: String): ManualTenderVariance?
    fun save(item: ManualTenderVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualTenderVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualTenderVarianceStore(
    var items: MutableMap<String, ManualTenderVariance>,
    var order: MutableList<String>,
) : ManualTenderVarianceStore {
    override fun find(code: String): ManualTenderVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualTenderVariance) {
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

    override fun list(): MutableList<ManualTenderVariance> {
        val out = mutableListOf<ManualTenderVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualTenderVarianceStore(): ManualTenderVarianceStore {
    return MemoryManualTenderVarianceStore(
        mutableMapOf<String, ManualTenderVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualTenderVarianceStore(
    var inner: ManualTenderVarianceStore,
    var cache: TtlCache<ManualTenderVariance>,
    var hits: Int,
    var misses: Int,
) : ManualTenderVarianceStore {
    override fun find(code: String): ManualTenderVariance? {
        val hit = (cache.get(code) as ManualTenderVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualTenderVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualTenderVariance> = inner.list()
}

fun newCachedManualTenderVarianceStore(inner: ManualTenderVarianceStore, ttl: Int, clock: Clock): ManualTenderVarianceStore {
    return CachedManualTenderVarianceStore(
        inner = inner,
        cache = newTtlCache<ManualTenderVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual tender variances. Empty fields match all. */
data class ManualTenderVarianceQuery(
    var region: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualTenderVariance): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualTenderVariance>): MutableList<ManualTenderVariance> {
        val matched = filter(items) { candidate: ManualTenderVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualTenderVariances(store: ManualTenderVarianceStore, query: ManualTenderVarianceQuery): MutableList<ManualTenderVariance> {
    return query.apply(store.list())
}
