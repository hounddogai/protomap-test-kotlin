package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery rollups by code. */
interface ProofOfDeliveryRollupStore {
    fun find(code: String): ProofOfDeliveryRollup?
    fun save(item: ProofOfDeliveryRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryRollupStore(
    var items: MutableMap<String, ProofOfDeliveryRollup>,
    var order: MutableList<String>,
) : ProofOfDeliveryRollupStore {
    override fun find(code: String): ProofOfDeliveryRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryRollup) {
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

    override fun list(): MutableList<ProofOfDeliveryRollup> {
        val out = mutableListOf<ProofOfDeliveryRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryRollupStore(): ProofOfDeliveryRollupStore {
    return MemoryProofOfDeliveryRollupStore(
        mutableMapOf<String, ProofOfDeliveryRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryRollupStore(
    var inner: ProofOfDeliveryRollupStore,
    var cache: TtlCache<ProofOfDeliveryRollup>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryRollupStore {
    override fun find(code: String): ProofOfDeliveryRollup? {
        val hit = (cache.get(code) as ProofOfDeliveryRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryRollup> = inner.list()
}

fun newCachedProofOfDeliveryRollupStore(inner: ProofOfDeliveryRollupStore, ttl: Int, clock: Clock): ProofOfDeliveryRollupStore {
    return CachedProofOfDeliveryRollupStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryRollup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery rollups. Empty fields match all. */
data class ProofOfDeliveryRollupQuery(
    var region: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryRollup): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryRollup>): MutableList<ProofOfDeliveryRollup> {
        val matched = filter(items) { candidate: ProofOfDeliveryRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryRollups(store: ProofOfDeliveryRollupStore, query: ProofOfDeliveryRollupQuery): MutableList<ProofOfDeliveryRollup> {
    return query.apply(store.list())
}
