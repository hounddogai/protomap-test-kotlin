package com.acme.transport.freightpay.dispute.recorditem

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped dispute claims by code. */
interface CappedDisputeClaimStore {
    fun find(code: String): CappedDisputeClaim?
    fun save(item: CappedDisputeClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedDisputeClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedDisputeClaimStore(
    var items: MutableMap<String, CappedDisputeClaim>,
    var order: MutableList<String>,
) : CappedDisputeClaimStore {
    override fun find(code: String): CappedDisputeClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedDisputeClaim) {
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

    override fun list(): MutableList<CappedDisputeClaim> {
        val out = mutableListOf<CappedDisputeClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedDisputeClaimStore(): CappedDisputeClaimStore {
    return MemoryCappedDisputeClaimStore(mutableMapOf<String, CappedDisputeClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedDisputeClaimStore(
    var inner: CappedDisputeClaimStore,
    var cache: TtlCache<CappedDisputeClaim>,
    var hits: Int,
    var misses: Int,
) : CappedDisputeClaimStore {
    override fun find(code: String): CappedDisputeClaim? {
        val hit = (cache.get(code) as CappedDisputeClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedDisputeClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedDisputeClaim> = inner.list()
}

fun newCachedCappedDisputeClaimStore(inner: CappedDisputeClaimStore, ttl: Int, clock: Clock): CappedDisputeClaimStore {
    return CachedCappedDisputeClaimStore(
        inner = inner,
        cache = newTtlCache<CappedDisputeClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped dispute claims. Empty fields match all. */
data class CappedDisputeClaimQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedDisputeClaim): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedDisputeClaim>): MutableList<CappedDisputeClaim> {
        val matched = filter(items) { candidate: CappedDisputeClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedDisputeClaims(store: CappedDisputeClaimStore, query: CappedDisputeClaimQuery): MutableList<CappedDisputeClaim> {
    return query.apply(store.list())
}
