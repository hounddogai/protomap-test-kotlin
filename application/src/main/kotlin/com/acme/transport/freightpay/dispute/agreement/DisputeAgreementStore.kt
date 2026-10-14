package com.acme.transport.freightpay.dispute.agreement

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute agreements by code. */
interface DisputeAgreementStore {
    fun find(code: String): DisputeAgreement?
    fun save(item: DisputeAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeAgreementStore(
    var items: MutableMap<String, DisputeAgreement>,
    var order: MutableList<String>,
) : DisputeAgreementStore {
    override fun find(code: String): DisputeAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeAgreement) {
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

    override fun list(): MutableList<DisputeAgreement> {
        val out = mutableListOf<DisputeAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeAgreementStore(): DisputeAgreementStore {
    return MemoryDisputeAgreementStore(mutableMapOf<String, DisputeAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeAgreementStore(
    var inner: DisputeAgreementStore,
    var cache: TtlCache<DisputeAgreement>,
    var hits: Int,
    var misses: Int,
) : DisputeAgreementStore {
    override fun find(code: String): DisputeAgreement? {
        val hit = (cache.get(code) as DisputeAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeAgreement> = inner.list()
}

fun newCachedDisputeAgreementStore(inner: DisputeAgreementStore, ttl: Int, clock: Clock): DisputeAgreementStore {
    return CachedDisputeAgreementStore(
        inner = inner,
        cache = newTtlCache<DisputeAgreement>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute agreements. Empty fields match all. */
data class DisputeAgreementQuery(
    var title: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeAgreement): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeAgreement>): MutableList<DisputeAgreement> {
        val matched = filter(items) { candidate: DisputeAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeAgreements(store: DisputeAgreementStore, query: DisputeAgreementQuery): MutableList<DisputeAgreement> {
    return query.apply(store.list())
}
