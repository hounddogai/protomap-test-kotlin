package com.acme.transport.freightpay.dispute.batcheddisputepolicy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient dispute agreements by code. */
interface LenientDisputeAgreementStore {
    fun find(code: String): LenientDisputeAgreement?
    fun save(item: LenientDisputeAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDisputeAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDisputeAgreementStore(
    var items: MutableMap<String, LenientDisputeAgreement>,
    var order: MutableList<String>,
) : LenientDisputeAgreementStore {
    override fun find(code: String): LenientDisputeAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDisputeAgreement) {
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

    override fun list(): MutableList<LenientDisputeAgreement> {
        val out = mutableListOf<LenientDisputeAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDisputeAgreementStore(): LenientDisputeAgreementStore {
    return MemoryLenientDisputeAgreementStore(
        mutableMapOf<String, LenientDisputeAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDisputeAgreementStore(
    var inner: LenientDisputeAgreementStore,
    var cache: TtlCache<LenientDisputeAgreement>,
    var hits: Int,
    var misses: Int,
) : LenientDisputeAgreementStore {
    override fun find(code: String): LenientDisputeAgreement? {
        val hit = (cache.get(code) as LenientDisputeAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDisputeAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDisputeAgreement> = inner.list()
}

fun newCachedLenientDisputeAgreementStore(inner: LenientDisputeAgreementStore, ttl: Int, clock: Clock): LenientDisputeAgreementStore {
    return CachedLenientDisputeAgreementStore(
        inner = inner,
        cache = newTtlCache<LenientDisputeAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient dispute agreements. Empty fields match all. */
data class LenientDisputeAgreementQuery(
    var name: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDisputeAgreement): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientDisputeAgreement>): MutableList<LenientDisputeAgreement> {
        val matched = filter(items) { candidate: LenientDisputeAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDisputeAgreements(store: LenientDisputeAgreementStore, query: LenientDisputeAgreementQuery): MutableList<LenientDisputeAgreement> {
    return query.apply(store.list())
}
