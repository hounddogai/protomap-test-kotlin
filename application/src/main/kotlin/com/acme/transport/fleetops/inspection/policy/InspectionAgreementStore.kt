package com.acme.transport.fleetops.inspection.policy

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection agreements by code. */
interface InspectionAgreementStore {
    fun find(code: String): InspectionAgreement?
    fun save(item: InspectionAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionAgreementStore(
    var items: MutableMap<String, InspectionAgreement>,
    var order: MutableList<String>,
) : InspectionAgreementStore {
    override fun find(code: String): InspectionAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionAgreement) {
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

    override fun list(): MutableList<InspectionAgreement> {
        val out = mutableListOf<InspectionAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionAgreementStore(): InspectionAgreementStore {
    return MemoryInspectionAgreementStore(mutableMapOf<String, InspectionAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionAgreementStore(
    var inner: InspectionAgreementStore,
    var cache: TtlCache<InspectionAgreement>,
    var hits: Int,
    var misses: Int,
) : InspectionAgreementStore {
    override fun find(code: String): InspectionAgreement? {
        val hit = (cache.get(code) as InspectionAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionAgreement> = inner.list()
}

fun newCachedInspectionAgreementStore(inner: InspectionAgreementStore, ttl: Int, clock: Clock): InspectionAgreementStore {
    return CachedInspectionAgreementStore(
        inner = inner,
        cache = newTtlCache<InspectionAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection agreements. Empty fields match all. */
data class InspectionAgreementQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionAgreement): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionAgreement>): MutableList<InspectionAgreement> {
        val matched = filter(items) { candidate: InspectionAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionAgreements(store: InspectionAgreementStore, query: InspectionAgreementQuery): MutableList<InspectionAgreement> {
    return query.apply(store.list())
}
