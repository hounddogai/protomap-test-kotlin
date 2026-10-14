package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute variances by code. */
interface DisputeVarianceStore {
    fun find(code: String): DisputeVariance?
    fun save(item: DisputeVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeVarianceStore(
    var items: MutableMap<String, DisputeVariance>,
    var order: MutableList<String>,
) : DisputeVarianceStore {
    override fun find(code: String): DisputeVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeVariance) {
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

    override fun list(): MutableList<DisputeVariance> {
        val out = mutableListOf<DisputeVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeVarianceStore(): DisputeVarianceStore {
    return MemoryDisputeVarianceStore(mutableMapOf<String, DisputeVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeVarianceStore(
    var inner: DisputeVarianceStore,
    var cache: TtlCache<DisputeVariance>,
    var hits: Int,
    var misses: Int,
) : DisputeVarianceStore {
    override fun find(code: String): DisputeVariance? {
        val hit = (cache.get(code) as DisputeVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeVariance> = inner.list()
}

fun newCachedDisputeVarianceStore(inner: DisputeVarianceStore, ttl: Int, clock: Clock): DisputeVarianceStore {
    return CachedDisputeVarianceStore(
        inner = inner,
        cache = newTtlCache<DisputeVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute variances. Empty fields match all. */
data class DisputeVarianceQuery(
    var name: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeVariance): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeVariance>): MutableList<DisputeVariance> {
        val matched = filter(items) { candidate: DisputeVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeVariances(store: DisputeVarianceStore, query: DisputeVarianceQuery): MutableList<DisputeVariance> {
    return query.apply(store.list())
}
