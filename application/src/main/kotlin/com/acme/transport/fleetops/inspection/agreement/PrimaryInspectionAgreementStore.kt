package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary inspection agreements by code. */
interface PrimaryInspectionAgreementStore {
    fun find(code: String): PrimaryInspectionAgreement?
    fun save(item: PrimaryInspectionAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryInspectionAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryInspectionAgreementStore(
    var items: MutableMap<String, PrimaryInspectionAgreement>,
    var order: MutableList<String>,
) : PrimaryInspectionAgreementStore {
    override fun find(code: String): PrimaryInspectionAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryInspectionAgreement) {
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

    override fun list(): MutableList<PrimaryInspectionAgreement> {
        val out = mutableListOf<PrimaryInspectionAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryInspectionAgreementStore(): PrimaryInspectionAgreementStore {
    return MemoryPrimaryInspectionAgreementStore(
        mutableMapOf<String, PrimaryInspectionAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryInspectionAgreementStore(
    var inner: PrimaryInspectionAgreementStore,
    var cache: TtlCache<PrimaryInspectionAgreement>,
    var hits: Int,
    var misses: Int,
) : PrimaryInspectionAgreementStore {
    override fun find(code: String): PrimaryInspectionAgreement? {
        val hit = (cache.get(code) as PrimaryInspectionAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryInspectionAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryInspectionAgreement> = inner.list()
}

fun newCachedPrimaryInspectionAgreementStore(inner: PrimaryInspectionAgreementStore, ttl: Int, clock: Clock): PrimaryInspectionAgreementStore {
    return CachedPrimaryInspectionAgreementStore(
        inner = inner,
        cache = newTtlCache<PrimaryInspectionAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary inspection agreements. Empty fields match all. */
data class PrimaryInspectionAgreementQuery(
    var source: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryInspectionAgreement): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryInspectionAgreement>): MutableList<PrimaryInspectionAgreement> {
        val matched = filter(items) { candidate: PrimaryInspectionAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryInspectionAgreements(store: PrimaryInspectionAgreementStore, query: PrimaryInspectionAgreementQuery): MutableList<PrimaryInspectionAgreement> {
    return query.apply(store.list())
}
