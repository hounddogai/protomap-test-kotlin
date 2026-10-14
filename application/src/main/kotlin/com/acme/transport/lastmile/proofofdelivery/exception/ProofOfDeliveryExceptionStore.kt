package com.acme.transport.lastmile.proofofdelivery.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery exceptions by code. */
interface ProofOfDeliveryExceptionStore {
    fun find(code: String): ProofOfDeliveryException?
    fun save(item: ProofOfDeliveryException)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryExceptionStore(
    var items: MutableMap<String, ProofOfDeliveryException>,
    var order: MutableList<String>,
) : ProofOfDeliveryExceptionStore {
    override fun find(code: String): ProofOfDeliveryException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryException) {
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

    override fun list(): MutableList<ProofOfDeliveryException> {
        val out = mutableListOf<ProofOfDeliveryException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryExceptionStore(): ProofOfDeliveryExceptionStore {
    return MemoryProofOfDeliveryExceptionStore(
        mutableMapOf<String, ProofOfDeliveryException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryExceptionStore(
    var inner: ProofOfDeliveryExceptionStore,
    var cache: TtlCache<ProofOfDeliveryException>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryExceptionStore {
    override fun find(code: String): ProofOfDeliveryException? {
        val hit = (cache.get(code) as ProofOfDeliveryException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryException> = inner.list()
}

fun newCachedProofOfDeliveryExceptionStore(inner: ProofOfDeliveryExceptionStore, ttl: Int, clock: Clock): ProofOfDeliveryExceptionStore {
    return CachedProofOfDeliveryExceptionStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryException>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery exceptions. Empty fields match all. */
data class ProofOfDeliveryExceptionQuery(
    var category: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryException): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryException>): MutableList<ProofOfDeliveryException> {
        val matched = filter(items) { candidate: ProofOfDeliveryException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryExceptions(store: ProofOfDeliveryExceptionStore, query: ProofOfDeliveryExceptionQuery): MutableList<ProofOfDeliveryException> {
    return query.apply(store.list())
}
