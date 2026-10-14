package com.acme.transport.crewing.qualification.assignment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification agreements by code. */
interface QualificationAgreementStore {
    fun find(code: String): QualificationAgreement?
    fun save(item: QualificationAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationAgreementStore(
    var items: MutableMap<String, QualificationAgreement>,
    var order: MutableList<String>,
) : QualificationAgreementStore {
    override fun find(code: String): QualificationAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationAgreement) {
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

    override fun list(): MutableList<QualificationAgreement> {
        val out = mutableListOf<QualificationAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationAgreementStore(): QualificationAgreementStore {
    return MemoryQualificationAgreementStore(
        mutableMapOf<String, QualificationAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationAgreementStore(
    var inner: QualificationAgreementStore,
    var cache: TtlCache<QualificationAgreement>,
    var hits: Int,
    var misses: Int,
) : QualificationAgreementStore {
    override fun find(code: String): QualificationAgreement? {
        val hit = (cache.get(code) as QualificationAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationAgreement> = inner.list()
}

fun newCachedQualificationAgreementStore(inner: QualificationAgreementStore, ttl: Int, clock: Clock): QualificationAgreementStore {
    return CachedQualificationAgreementStore(
        inner = inner,
        cache = newTtlCache<QualificationAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification agreements. Empty fields match all. */
data class QualificationAgreementQuery(
    var source: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationAgreement): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<QualificationAgreement>): MutableList<QualificationAgreement> {
        val matched = filter(items) { candidate: QualificationAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationAgreements(store: QualificationAgreementStore, query: QualificationAgreementQuery): MutableList<QualificationAgreement> {
    return query.apply(store.list())
}
