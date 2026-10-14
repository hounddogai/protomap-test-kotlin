package com.acme.transport.crewing.swap.swapmapping

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped swap agreements by code. */
interface CappedSwapAgreementStore {
    fun find(code: String): CappedSwapAgreement?
    fun save(item: CappedSwapAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedSwapAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedSwapAgreementStore(
    var items: MutableMap<String, CappedSwapAgreement>,
    var order: MutableList<String>,
) : CappedSwapAgreementStore {
    override fun find(code: String): CappedSwapAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedSwapAgreement) {
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

    override fun list(): MutableList<CappedSwapAgreement> {
        val out = mutableListOf<CappedSwapAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedSwapAgreementStore(): CappedSwapAgreementStore {
    return MemoryCappedSwapAgreementStore(mutableMapOf<String, CappedSwapAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedSwapAgreementStore(
    var inner: CappedSwapAgreementStore,
    var cache: TtlCache<CappedSwapAgreement>,
    var hits: Int,
    var misses: Int,
) : CappedSwapAgreementStore {
    override fun find(code: String): CappedSwapAgreement? {
        val hit = (cache.get(code) as CappedSwapAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedSwapAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedSwapAgreement> = inner.list()
}

fun newCachedCappedSwapAgreementStore(inner: CappedSwapAgreementStore, ttl: Int, clock: Clock): CappedSwapAgreementStore {
    return CachedCappedSwapAgreementStore(
        inner = inner,
        cache = newTtlCache<CappedSwapAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped swap agreements. Empty fields match all. */
data class CappedSwapAgreementQuery(
    var country: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedSwapAgreement): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<CappedSwapAgreement>): MutableList<CappedSwapAgreement> {
        val matched = filter(items) { candidate: CappedSwapAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedSwapAgreements(store: CappedSwapAgreementStore, query: CappedSwapAgreementQuery): MutableList<CappedSwapAgreement> {
    return query.apply(store.list())
}
