package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery bundles by code. */
interface ProofOfDeliveryBundleStore {
    fun find(code: String): ProofOfDeliveryBundle?
    fun save(item: ProofOfDeliveryBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryBundleStore(
    var items: MutableMap<String, ProofOfDeliveryBundle>,
    var order: MutableList<String>,
) : ProofOfDeliveryBundleStore {
    override fun find(code: String): ProofOfDeliveryBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryBundle) {
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

    override fun list(): MutableList<ProofOfDeliveryBundle> {
        val out = mutableListOf<ProofOfDeliveryBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryBundleStore(): ProofOfDeliveryBundleStore {
    return MemoryProofOfDeliveryBundleStore(
        mutableMapOf<String, ProofOfDeliveryBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryBundleStore(
    var inner: ProofOfDeliveryBundleStore,
    var cache: TtlCache<ProofOfDeliveryBundle>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryBundleStore {
    override fun find(code: String): ProofOfDeliveryBundle? {
        val hit = (cache.get(code) as ProofOfDeliveryBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryBundle> = inner.list()
}

fun newCachedProofOfDeliveryBundleStore(inner: ProofOfDeliveryBundleStore, ttl: Int, clock: Clock): ProofOfDeliveryBundleStore {
    return CachedProofOfDeliveryBundleStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryBundle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery bundles. Empty fields match all. */
data class ProofOfDeliveryBundleQuery(
    var depot: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryBundle): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryBundle>): MutableList<ProofOfDeliveryBundle> {
        val matched = filter(items) { candidate: ProofOfDeliveryBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryBundles(store: ProofOfDeliveryBundleStore, query: ProofOfDeliveryBundleQuery): MutableList<ProofOfDeliveryBundle> {
    return query.apply(store.list())
}
