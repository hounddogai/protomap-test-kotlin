package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy defect agreements by code. */
interface LegacyDefectAgreementStore {
    fun find(code: String): LegacyDefectAgreement?
    fun save(item: LegacyDefectAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyDefectAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyDefectAgreementStore(
    var items: MutableMap<String, LegacyDefectAgreement>,
    var order: MutableList<String>,
) : LegacyDefectAgreementStore {
    override fun find(code: String): LegacyDefectAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyDefectAgreement) {
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

    override fun list(): MutableList<LegacyDefectAgreement> {
        val out = mutableListOf<LegacyDefectAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyDefectAgreementStore(): LegacyDefectAgreementStore {
    return MemoryLegacyDefectAgreementStore(
        mutableMapOf<String, LegacyDefectAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyDefectAgreementStore(
    var inner: LegacyDefectAgreementStore,
    var cache: TtlCache<LegacyDefectAgreement>,
    var hits: Int,
    var misses: Int,
) : LegacyDefectAgreementStore {
    override fun find(code: String): LegacyDefectAgreement? {
        val hit = (cache.get(code) as LegacyDefectAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyDefectAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyDefectAgreement> = inner.list()
}

fun newCachedLegacyDefectAgreementStore(inner: LegacyDefectAgreementStore, ttl: Int, clock: Clock): LegacyDefectAgreementStore {
    return CachedLegacyDefectAgreementStore(
        inner = inner,
        cache = newTtlCache<LegacyDefectAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy defect agreements. Empty fields match all. */
data class LegacyDefectAgreementQuery(
    var locale: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyDefectAgreement): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<LegacyDefectAgreement>): MutableList<LegacyDefectAgreement> {
        val matched = filter(items) { candidate: LegacyDefectAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyDefectAgreements(store: LegacyDefectAgreementStore, query: LegacyDefectAgreementQuery): MutableList<LegacyDefectAgreement> {
    return query.apply(store.list())
}
