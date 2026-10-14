package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped trailer agreements by code. */
interface CappedTrailerAgreementStore {
    fun find(code: String): CappedTrailerAgreement?
    fun save(item: CappedTrailerAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedTrailerAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedTrailerAgreementStore(
    var items: MutableMap<String, CappedTrailerAgreement>,
    var order: MutableList<String>,
) : CappedTrailerAgreementStore {
    override fun find(code: String): CappedTrailerAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedTrailerAgreement) {
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

    override fun list(): MutableList<CappedTrailerAgreement> {
        val out = mutableListOf<CappedTrailerAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedTrailerAgreementStore(): CappedTrailerAgreementStore {
    return MemoryCappedTrailerAgreementStore(
        mutableMapOf<String, CappedTrailerAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedTrailerAgreementStore(
    var inner: CappedTrailerAgreementStore,
    var cache: TtlCache<CappedTrailerAgreement>,
    var hits: Int,
    var misses: Int,
) : CappedTrailerAgreementStore {
    override fun find(code: String): CappedTrailerAgreement? {
        val hit = (cache.get(code) as CappedTrailerAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedTrailerAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedTrailerAgreement> = inner.list()
}

fun newCachedCappedTrailerAgreementStore(inner: CappedTrailerAgreementStore, ttl: Int, clock: Clock): CappedTrailerAgreementStore {
    return CachedCappedTrailerAgreementStore(
        inner = inner,
        cache = newTtlCache<CappedTrailerAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped trailer agreements. Empty fields match all. */
data class CappedTrailerAgreementQuery(
    var segment: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedTrailerAgreement): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedTrailerAgreement>): MutableList<CappedTrailerAgreement> {
        val matched = filter(items) { candidate: CappedTrailerAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedTrailerAgreements(store: CappedTrailerAgreementStore, query: CappedTrailerAgreementQuery): MutableList<CappedTrailerAgreement> {
    return query.apply(store.list())
}
