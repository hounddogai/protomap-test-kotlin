package com.acme.transport.lastmile.proofofdelivery.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery targets by code. */
interface ProofOfDeliveryTargetStore {
    fun find(code: String): ProofOfDeliveryTarget?
    fun save(item: ProofOfDeliveryTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryTargetStore(
    var items: MutableMap<String, ProofOfDeliveryTarget>,
    var order: MutableList<String>,
) : ProofOfDeliveryTargetStore {
    override fun find(code: String): ProofOfDeliveryTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryTarget) {
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

    override fun list(): MutableList<ProofOfDeliveryTarget> {
        val out = mutableListOf<ProofOfDeliveryTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryTargetStore(): ProofOfDeliveryTargetStore {
    return MemoryProofOfDeliveryTargetStore(
        mutableMapOf<String, ProofOfDeliveryTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryTargetStore(
    var inner: ProofOfDeliveryTargetStore,
    var cache: TtlCache<ProofOfDeliveryTarget>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryTargetStore {
    override fun find(code: String): ProofOfDeliveryTarget? {
        val hit = (cache.get(code) as ProofOfDeliveryTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryTarget> = inner.list()
}

fun newCachedProofOfDeliveryTargetStore(inner: ProofOfDeliveryTargetStore, ttl: Int, clock: Clock): ProofOfDeliveryTargetStore {
    return CachedProofOfDeliveryTargetStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryTarget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery targets. Empty fields match all. */
data class ProofOfDeliveryTargetQuery(
    var depot: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryTarget): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryTarget>): MutableList<ProofOfDeliveryTarget> {
        val matched = filter(items) { candidate: ProofOfDeliveryTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryTargets(store: ProofOfDeliveryTargetStore, query: ProofOfDeliveryTargetQuery): MutableList<ProofOfDeliveryTarget> {
    return query.apply(store.list())
}
