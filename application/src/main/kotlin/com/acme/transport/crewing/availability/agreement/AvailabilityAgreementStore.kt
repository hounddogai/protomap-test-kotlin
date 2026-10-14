package com.acme.transport.crewing.availability.agreement

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability agreements by code. */
interface AvailabilityAgreementStore {
    fun find(code: String): AvailabilityAgreement?
    fun save(item: AvailabilityAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityAgreementStore(
    var items: MutableMap<String, AvailabilityAgreement>,
    var order: MutableList<String>,
) : AvailabilityAgreementStore {
    override fun find(code: String): AvailabilityAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityAgreement) {
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

    override fun list(): MutableList<AvailabilityAgreement> {
        val out = mutableListOf<AvailabilityAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityAgreementStore(): AvailabilityAgreementStore {
    return MemoryAvailabilityAgreementStore(
        mutableMapOf<String, AvailabilityAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityAgreementStore(
    var inner: AvailabilityAgreementStore,
    var cache: TtlCache<AvailabilityAgreement>,
    var hits: Int,
    var misses: Int,
) : AvailabilityAgreementStore {
    override fun find(code: String): AvailabilityAgreement? {
        val hit = (cache.get(code) as AvailabilityAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityAgreement> = inner.list()
}

fun newCachedAvailabilityAgreementStore(inner: AvailabilityAgreementStore, ttl: Int, clock: Clock): AvailabilityAgreementStore {
    return CachedAvailabilityAgreementStore(
        inner = inner,
        cache = newTtlCache<AvailabilityAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability agreements. Empty fields match all. */
data class AvailabilityAgreementQuery(
    var lane: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityAgreement): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<AvailabilityAgreement>): MutableList<AvailabilityAgreement> {
        val matched = filter(items) { candidate: AvailabilityAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityAgreements(store: AvailabilityAgreementStore, query: AvailabilityAgreementQuery): MutableList<AvailabilityAgreement> {
    return query.apply(store.list())
}
