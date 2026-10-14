package com.acme.transport.crewing.qualification.digest

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped qualification agreements by code. */
interface CappedQualificationAgreementStore {
    fun find(code: String): CappedQualificationAgreement?
    fun save(item: CappedQualificationAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedQualificationAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedQualificationAgreementStore(
    var items: MutableMap<String, CappedQualificationAgreement>,
    var order: MutableList<String>,
) : CappedQualificationAgreementStore {
    override fun find(code: String): CappedQualificationAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedQualificationAgreement) {
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

    override fun list(): MutableList<CappedQualificationAgreement> {
        val out = mutableListOf<CappedQualificationAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedQualificationAgreementStore(): CappedQualificationAgreementStore {
    return MemoryCappedQualificationAgreementStore(
        mutableMapOf<String, CappedQualificationAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedQualificationAgreementStore(
    var inner: CappedQualificationAgreementStore,
    var cache: TtlCache<CappedQualificationAgreement>,
    var hits: Int,
    var misses: Int,
) : CappedQualificationAgreementStore {
    override fun find(code: String): CappedQualificationAgreement? {
        val hit = (cache.get(code) as CappedQualificationAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedQualificationAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedQualificationAgreement> = inner.list()
}

fun newCachedCappedQualificationAgreementStore(inner: CappedQualificationAgreementStore, ttl: Int, clock: Clock): CappedQualificationAgreementStore {
    return CachedCappedQualificationAgreementStore(
        inner = inner,
        cache = newTtlCache<CappedQualificationAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped qualification agreements. Empty fields match all. */
data class CappedQualificationAgreementQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedQualificationAgreement): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedQualificationAgreement>): MutableList<CappedQualificationAgreement> {
        val matched = filter(items) { candidate: CappedQualificationAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedQualificationAgreements(store: CappedQualificationAgreementStore, query: CappedQualificationAgreementQuery): MutableList<CappedQualificationAgreement> {
    return query.apply(store.list())
}
