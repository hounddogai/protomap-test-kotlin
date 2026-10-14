package com.acme.transport.freightpay.dispute.adjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute thresholds by code. */
interface DisputeThresholdStore {
    fun find(code: String): DisputeThreshold?
    fun save(item: DisputeThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeThresholdStore(
    var items: MutableMap<String, DisputeThreshold>,
    var order: MutableList<String>,
) : DisputeThresholdStore {
    override fun find(code: String): DisputeThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeThreshold) {
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

    override fun list(): MutableList<DisputeThreshold> {
        val out = mutableListOf<DisputeThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeThresholdStore(): DisputeThresholdStore {
    return MemoryDisputeThresholdStore(mutableMapOf<String, DisputeThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeThresholdStore(
    var inner: DisputeThresholdStore,
    var cache: TtlCache<DisputeThreshold>,
    var hits: Int,
    var misses: Int,
) : DisputeThresholdStore {
    override fun find(code: String): DisputeThreshold? {
        val hit = (cache.get(code) as DisputeThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeThreshold> = inner.list()
}

fun newCachedDisputeThresholdStore(inner: DisputeThresholdStore, ttl: Int, clock: Clock): DisputeThresholdStore {
    return CachedDisputeThresholdStore(
        inner = inner,
        cache = newTtlCache<DisputeThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute thresholds. Empty fields match all. */
data class DisputeThresholdQuery(
    var depot: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeThreshold): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeThreshold>): MutableList<DisputeThreshold> {
        val matched = filter(items) { candidate: DisputeThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeThresholds(store: DisputeThresholdStore, query: DisputeThresholdQuery): MutableList<DisputeThreshold> {
    return query.apply(store.list())
}
