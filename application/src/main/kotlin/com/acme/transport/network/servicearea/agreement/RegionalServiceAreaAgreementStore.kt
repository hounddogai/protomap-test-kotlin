package com.acme.transport.network.servicearea.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional service area agreements by code. */
interface RegionalServiceAreaAgreementStore {
    fun find(code: String): RegionalServiceAreaAgreement?
    fun save(item: RegionalServiceAreaAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalServiceAreaAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalServiceAreaAgreementStore(
    var items: MutableMap<String, RegionalServiceAreaAgreement>,
    var order: MutableList<String>,
) : RegionalServiceAreaAgreementStore {
    override fun find(code: String): RegionalServiceAreaAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalServiceAreaAgreement) {
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

    override fun list(): MutableList<RegionalServiceAreaAgreement> {
        val out = mutableListOf<RegionalServiceAreaAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalServiceAreaAgreementStore(): RegionalServiceAreaAgreementStore {
    return MemoryRegionalServiceAreaAgreementStore(
        mutableMapOf<String, RegionalServiceAreaAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalServiceAreaAgreementStore(
    var inner: RegionalServiceAreaAgreementStore,
    var cache: TtlCache<RegionalServiceAreaAgreement>,
    var hits: Int,
    var misses: Int,
) : RegionalServiceAreaAgreementStore {
    override fun find(code: String): RegionalServiceAreaAgreement? {
        val hit = (cache.get(code) as RegionalServiceAreaAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalServiceAreaAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalServiceAreaAgreement> = inner.list()
}

fun newCachedRegionalServiceAreaAgreementStore(inner: RegionalServiceAreaAgreementStore, ttl: Int, clock: Clock): RegionalServiceAreaAgreementStore {
    return CachedRegionalServiceAreaAgreementStore(
        inner = inner,
        cache = newTtlCache<RegionalServiceAreaAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional service area agreements. Empty fields match all. */
data class RegionalServiceAreaAgreementQuery(
    var name: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalServiceAreaAgreement): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<RegionalServiceAreaAgreement>): MutableList<RegionalServiceAreaAgreement> {
        val matched = filter(items) { candidate: RegionalServiceAreaAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalServiceAreaAgreements(store: RegionalServiceAreaAgreementStore, query: RegionalServiceAreaAgreementQuery): MutableList<RegionalServiceAreaAgreement> {
    return query.apply(store.list())
}
