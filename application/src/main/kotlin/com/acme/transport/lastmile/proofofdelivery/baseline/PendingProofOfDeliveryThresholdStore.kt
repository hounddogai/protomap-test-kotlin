package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps pending proof of delivery thresholds by code. */
interface PendingProofOfDeliveryThresholdStore {
    fun find(code: String): PendingProofOfDeliveryThreshold?
    fun save(item: PendingProofOfDeliveryThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingProofOfDeliveryThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingProofOfDeliveryThresholdStore(
    var items: MutableMap<String, PendingProofOfDeliveryThreshold>,
    var order: MutableList<String>,
) : PendingProofOfDeliveryThresholdStore {
    override fun find(code: String): PendingProofOfDeliveryThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingProofOfDeliveryThreshold) {
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

    override fun list(): MutableList<PendingProofOfDeliveryThreshold> {
        val out = mutableListOf<PendingProofOfDeliveryThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingProofOfDeliveryThresholdStore(): PendingProofOfDeliveryThresholdStore {
    return MemoryPendingProofOfDeliveryThresholdStore(
        mutableMapOf<String, PendingProofOfDeliveryThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingProofOfDeliveryThresholdStore(
    var inner: PendingProofOfDeliveryThresholdStore,
    var cache: TtlCache<PendingProofOfDeliveryThreshold>,
    var hits: Int,
    var misses: Int,
) : PendingProofOfDeliveryThresholdStore {
    override fun find(code: String): PendingProofOfDeliveryThreshold? {
        val hit = (cache.get(code) as PendingProofOfDeliveryThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingProofOfDeliveryThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingProofOfDeliveryThreshold> = inner.list()
}

fun newCachedPendingProofOfDeliveryThresholdStore(inner: PendingProofOfDeliveryThresholdStore, ttl: Int, clock: Clock): PendingProofOfDeliveryThresholdStore {
    return CachedPendingProofOfDeliveryThresholdStore(
        inner = inner,
        cache = newTtlCache<PendingProofOfDeliveryThreshold>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending proof of delivery thresholds. Empty fields match all. */
data class PendingProofOfDeliveryThresholdQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingProofOfDeliveryThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<PendingProofOfDeliveryThreshold>): MutableList<PendingProofOfDeliveryThreshold> {
        val matched = filter(items) { candidate: PendingProofOfDeliveryThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingProofOfDeliveryThresholds(store: PendingProofOfDeliveryThresholdStore, query: PendingProofOfDeliveryThresholdQuery): MutableList<PendingProofOfDeliveryThreshold> {
    return query.apply(store.list())
}
