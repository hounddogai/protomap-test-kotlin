package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender agreements by code. */
interface TenderAgreementStore {
    fun find(code: String): TenderAgreement?
    fun save(item: TenderAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderAgreementStore(
    var items: MutableMap<String, TenderAgreement>,
    var order: MutableList<String>,
) : TenderAgreementStore {
    override fun find(code: String): TenderAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderAgreement) {
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

    override fun list(): MutableList<TenderAgreement> {
        val out = mutableListOf<TenderAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderAgreementStore(): TenderAgreementStore {
    return MemoryTenderAgreementStore(mutableMapOf<String, TenderAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderAgreementStore(
    var inner: TenderAgreementStore,
    var cache: TtlCache<TenderAgreement>,
    var hits: Int,
    var misses: Int,
) : TenderAgreementStore {
    override fun find(code: String): TenderAgreement? {
        val hit = (cache.get(code) as TenderAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderAgreement> = inner.list()
}

fun newCachedTenderAgreementStore(inner: TenderAgreementStore, ttl: Int, clock: Clock): TenderAgreementStore {
    return CachedTenderAgreementStore(
        inner = inner,
        cache = newTtlCache<TenderAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender agreements. Empty fields match all. */
data class TenderAgreementQuery(
    var title: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderAgreement): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TenderAgreement>): MutableList<TenderAgreement> {
        val matched = filter(items) { candidate: TenderAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderAgreements(store: TenderAgreementStore, query: TenderAgreementQuery): MutableList<TenderAgreement> {
    return query.apply(store.list())
}
