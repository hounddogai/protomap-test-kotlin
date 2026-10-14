package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy rest break agreements by code. */
interface LegacyRestBreakAgreementStore {
    fun find(code: String): LegacyRestBreakAgreement?
    fun save(item: LegacyRestBreakAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyRestBreakAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyRestBreakAgreementStore(
    var items: MutableMap<String, LegacyRestBreakAgreement>,
    var order: MutableList<String>,
) : LegacyRestBreakAgreementStore {
    override fun find(code: String): LegacyRestBreakAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyRestBreakAgreement) {
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

    override fun list(): MutableList<LegacyRestBreakAgreement> {
        val out = mutableListOf<LegacyRestBreakAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyRestBreakAgreementStore(): LegacyRestBreakAgreementStore {
    return MemoryLegacyRestBreakAgreementStore(
        mutableMapOf<String, LegacyRestBreakAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyRestBreakAgreementStore(
    var inner: LegacyRestBreakAgreementStore,
    var cache: TtlCache<LegacyRestBreakAgreement>,
    var hits: Int,
    var misses: Int,
) : LegacyRestBreakAgreementStore {
    override fun find(code: String): LegacyRestBreakAgreement? {
        val hit = (cache.get(code) as LegacyRestBreakAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyRestBreakAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyRestBreakAgreement> = inner.list()
}

fun newCachedLegacyRestBreakAgreementStore(inner: LegacyRestBreakAgreementStore, ttl: Int, clock: Clock): LegacyRestBreakAgreementStore {
    return CachedLegacyRestBreakAgreementStore(
        inner = inner,
        cache = newTtlCache<LegacyRestBreakAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy rest break agreements. Empty fields match all. */
data class LegacyRestBreakAgreementQuery(
    var segment: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyRestBreakAgreement): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<LegacyRestBreakAgreement>): MutableList<LegacyRestBreakAgreement> {
        val matched = filter(items) { candidate: LegacyRestBreakAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyRestBreakAgreements(store: LegacyRestBreakAgreementStore, query: LegacyRestBreakAgreementQuery): MutableList<LegacyRestBreakAgreement> {
    return query.apply(store.list())
}
