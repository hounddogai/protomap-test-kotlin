package com.acme.transport.lastmile.proofofdelivery.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery thresholds by code. */
interface ProofOfDeliveryThresholdStore {
    fun find(code: String): ProofOfDeliveryThreshold?
    fun save(item: ProofOfDeliveryThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryThresholdStore(
    var items: MutableMap<String, ProofOfDeliveryThreshold>,
    var order: MutableList<String>,
) : ProofOfDeliveryThresholdStore {
    override fun find(code: String): ProofOfDeliveryThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryThreshold) {
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

    override fun list(): MutableList<ProofOfDeliveryThreshold> {
        val out = mutableListOf<ProofOfDeliveryThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryThresholdStore(): ProofOfDeliveryThresholdStore {
    return MemoryProofOfDeliveryThresholdStore(
        mutableMapOf<String, ProofOfDeliveryThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryThresholdStore(
    var inner: ProofOfDeliveryThresholdStore,
    var cache: TtlCache<ProofOfDeliveryThreshold>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryThresholdStore {
    override fun find(code: String): ProofOfDeliveryThreshold? {
        val hit = (cache.get(code) as ProofOfDeliveryThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryThreshold> = inner.list()
}

fun newCachedProofOfDeliveryThresholdStore(inner: ProofOfDeliveryThresholdStore, ttl: Int, clock: Clock): ProofOfDeliveryThresholdStore {
    return CachedProofOfDeliveryThresholdStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryThreshold>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery thresholds. Empty fields match all. */
data class ProofOfDeliveryThresholdQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryThreshold): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<ProofOfDeliveryThreshold>): MutableList<ProofOfDeliveryThreshold> {
        val matched = filter(items) { candidate: ProofOfDeliveryThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryThresholds(store: ProofOfDeliveryThresholdStore, query: ProofOfDeliveryThresholdQuery): MutableList<ProofOfDeliveryThreshold> {
    return query.apply(store.list())
}
