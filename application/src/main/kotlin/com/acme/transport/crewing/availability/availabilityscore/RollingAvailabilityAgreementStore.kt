package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling availability agreements by code. */
interface RollingAvailabilityAgreementStore {
    fun find(code: String): RollingAvailabilityAgreement?
    fun save(item: RollingAvailabilityAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingAvailabilityAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingAvailabilityAgreementStore(
    var items: MutableMap<String, RollingAvailabilityAgreement>,
    var order: MutableList<String>,
) : RollingAvailabilityAgreementStore {
    override fun find(code: String): RollingAvailabilityAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingAvailabilityAgreement) {
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

    override fun list(): MutableList<RollingAvailabilityAgreement> {
        val out = mutableListOf<RollingAvailabilityAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingAvailabilityAgreementStore(): RollingAvailabilityAgreementStore {
    return MemoryRollingAvailabilityAgreementStore(
        mutableMapOf<String, RollingAvailabilityAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingAvailabilityAgreementStore(
    var inner: RollingAvailabilityAgreementStore,
    var cache: TtlCache<RollingAvailabilityAgreement>,
    var hits: Int,
    var misses: Int,
) : RollingAvailabilityAgreementStore {
    override fun find(code: String): RollingAvailabilityAgreement? {
        val hit = (cache.get(code) as RollingAvailabilityAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingAvailabilityAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingAvailabilityAgreement> = inner.list()
}

fun newCachedRollingAvailabilityAgreementStore(inner: RollingAvailabilityAgreementStore, ttl: Int, clock: Clock): RollingAvailabilityAgreementStore {
    return CachedRollingAvailabilityAgreementStore(
        inner = inner,
        cache = newTtlCache<RollingAvailabilityAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling availability agreements. Empty fields match all. */
data class RollingAvailabilityAgreementQuery(
    var source: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingAvailabilityAgreement): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingAvailabilityAgreement>): MutableList<RollingAvailabilityAgreement> {
        val matched = filter(items) { candidate: RollingAvailabilityAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingAvailabilityAgreements(store: RollingAvailabilityAgreementStore, query: RollingAvailabilityAgreementQuery): MutableList<RollingAvailabilityAgreement> {
    return query.apply(store.list())
}
