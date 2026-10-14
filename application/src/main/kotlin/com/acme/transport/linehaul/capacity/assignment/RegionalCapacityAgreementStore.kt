package com.acme.transport.linehaul.capacity.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps regional capacity agreements by code. */
interface RegionalCapacityAgreementStore {
    fun find(code: String): RegionalCapacityAgreement?
    fun save(item: RegionalCapacityAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCapacityAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCapacityAgreementStore(
    var items: MutableMap<String, RegionalCapacityAgreement>,
    var order: MutableList<String>,
) : RegionalCapacityAgreementStore {
    override fun find(code: String): RegionalCapacityAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCapacityAgreement) {
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

    override fun list(): MutableList<RegionalCapacityAgreement> {
        val out = mutableListOf<RegionalCapacityAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCapacityAgreementStore(): RegionalCapacityAgreementStore {
    return MemoryRegionalCapacityAgreementStore(
        mutableMapOf<String, RegionalCapacityAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCapacityAgreementStore(
    var inner: RegionalCapacityAgreementStore,
    var cache: TtlCache<RegionalCapacityAgreement>,
    var hits: Int,
    var misses: Int,
) : RegionalCapacityAgreementStore {
    override fun find(code: String): RegionalCapacityAgreement? {
        val hit = (cache.get(code) as RegionalCapacityAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCapacityAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCapacityAgreement> = inner.list()
}

fun newCachedRegionalCapacityAgreementStore(inner: RegionalCapacityAgreementStore, ttl: Int, clock: Clock): RegionalCapacityAgreementStore {
    return CachedRegionalCapacityAgreementStore(
        inner = inner,
        cache = newTtlCache<RegionalCapacityAgreement>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional capacity agreements. Empty fields match all. */
data class RegionalCapacityAgreementQuery(
    var label: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCapacityAgreement): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalCapacityAgreement>): MutableList<RegionalCapacityAgreement> {
        val matched = filter(items) { candidate: RegionalCapacityAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCapacityAgreements(store: RegionalCapacityAgreementStore, query: RegionalCapacityAgreementQuery): MutableList<RegionalCapacityAgreement> {
    return query.apply(store.list())
}
