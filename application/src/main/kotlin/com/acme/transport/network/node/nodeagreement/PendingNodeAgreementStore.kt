package com.acme.transport.network.node.nodeagreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps pending node agreements by code. */
interface PendingNodeAgreementStore {
    fun find(code: String): PendingNodeAgreement?
    fun save(item: PendingNodeAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingNodeAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingNodeAgreementStore(
    var items: MutableMap<String, PendingNodeAgreement>,
    var order: MutableList<String>,
) : PendingNodeAgreementStore {
    override fun find(code: String): PendingNodeAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingNodeAgreement) {
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

    override fun list(): MutableList<PendingNodeAgreement> {
        val out = mutableListOf<PendingNodeAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingNodeAgreementStore(): PendingNodeAgreementStore {
    return MemoryPendingNodeAgreementStore(
        mutableMapOf<String, PendingNodeAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingNodeAgreementStore(
    var inner: PendingNodeAgreementStore,
    var cache: TtlCache<PendingNodeAgreement>,
    var hits: Int,
    var misses: Int,
) : PendingNodeAgreementStore {
    override fun find(code: String): PendingNodeAgreement? {
        val hit = (cache.get(code) as PendingNodeAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingNodeAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingNodeAgreement> = inner.list()
}

fun newCachedPendingNodeAgreementStore(inner: PendingNodeAgreementStore, ttl: Int, clock: Clock): PendingNodeAgreementStore {
    return CachedPendingNodeAgreementStore(
        inner = inner,
        cache = newTtlCache<PendingNodeAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending node agreements. Empty fields match all. */
data class PendingNodeAgreementQuery(
    var lane: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingNodeAgreement): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingNodeAgreement>): MutableList<PendingNodeAgreement> {
        val matched = filter(items) { candidate: PendingNodeAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingNodeAgreements(store: PendingNodeAgreementStore, query: PendingNodeAgreementQuery): MutableList<PendingNodeAgreement> {
    return query.apply(store.list())
}
