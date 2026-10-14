package com.acme.transport.crewing.swap.plan

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap agreements by code. */
interface SwapAgreementStore {
    fun find(code: String): SwapAgreement?
    fun save(item: SwapAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapAgreementStore(
    var items: MutableMap<String, SwapAgreement>,
    var order: MutableList<String>,
) : SwapAgreementStore {
    override fun find(code: String): SwapAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapAgreement) {
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

    override fun list(): MutableList<SwapAgreement> {
        val out = mutableListOf<SwapAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapAgreementStore(): SwapAgreementStore {
    return MemorySwapAgreementStore(mutableMapOf<String, SwapAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapAgreementStore(
    var inner: SwapAgreementStore,
    var cache: TtlCache<SwapAgreement>,
    var hits: Int,
    var misses: Int,
) : SwapAgreementStore {
    override fun find(code: String): SwapAgreement? {
        val hit = (cache.get(code) as SwapAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapAgreement> = inner.list()
}

fun newCachedSwapAgreementStore(inner: SwapAgreementStore, ttl: Int, clock: Clock): SwapAgreementStore {
    return CachedSwapAgreementStore(
        inner = inner,
        cache = newTtlCache<SwapAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap agreements. Empty fields match all. */
data class SwapAgreementQuery(
    var segment: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapAgreement): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapAgreement>): MutableList<SwapAgreement> {
        val matched = filter(items) { candidate: SwapAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapAgreements(store: SwapAgreementStore, query: SwapAgreementQuery): MutableList<SwapAgreement> {
    return query.apply(store.list())
}
