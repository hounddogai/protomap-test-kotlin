package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery agreements by code. */
interface ProofOfDeliveryAgreementStore {
    fun find(code: String): ProofOfDeliveryAgreement?
    fun save(item: ProofOfDeliveryAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryAgreementStore(
    var items: MutableMap<String, ProofOfDeliveryAgreement>,
    var order: MutableList<String>,
) : ProofOfDeliveryAgreementStore {
    override fun find(code: String): ProofOfDeliveryAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryAgreement) {
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

    override fun list(): MutableList<ProofOfDeliveryAgreement> {
        val out = mutableListOf<ProofOfDeliveryAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryAgreementStore(): ProofOfDeliveryAgreementStore {
    return MemoryProofOfDeliveryAgreementStore(
        mutableMapOf<String, ProofOfDeliveryAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryAgreementStore(
    var inner: ProofOfDeliveryAgreementStore,
    var cache: TtlCache<ProofOfDeliveryAgreement>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryAgreementStore {
    override fun find(code: String): ProofOfDeliveryAgreement? {
        val hit = (cache.get(code) as ProofOfDeliveryAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryAgreement> = inner.list()
}

fun newCachedProofOfDeliveryAgreementStore(inner: ProofOfDeliveryAgreementStore, ttl: Int, clock: Clock): ProofOfDeliveryAgreementStore {
    return CachedProofOfDeliveryAgreementStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery agreements. Empty fields match all. */
data class ProofOfDeliveryAgreementQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryAgreement): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryAgreement>): MutableList<ProofOfDeliveryAgreement> {
        val matched = filter(items) { candidate: ProofOfDeliveryAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryAgreements(store: ProofOfDeliveryAgreementStore, query: ProofOfDeliveryAgreementQuery): MutableList<ProofOfDeliveryAgreement> {
    return query.apply(store.list())
}
