package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling fuel burn agreements by code. */
interface RollingFuelBurnAgreementStore {
    fun find(code: String): RollingFuelBurnAgreement?
    fun save(item: RollingFuelBurnAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingFuelBurnAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingFuelBurnAgreementStore(
    var items: MutableMap<String, RollingFuelBurnAgreement>,
    var order: MutableList<String>,
) : RollingFuelBurnAgreementStore {
    override fun find(code: String): RollingFuelBurnAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingFuelBurnAgreement) {
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

    override fun list(): MutableList<RollingFuelBurnAgreement> {
        val out = mutableListOf<RollingFuelBurnAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingFuelBurnAgreementStore(): RollingFuelBurnAgreementStore {
    return MemoryRollingFuelBurnAgreementStore(
        mutableMapOf<String, RollingFuelBurnAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingFuelBurnAgreementStore(
    var inner: RollingFuelBurnAgreementStore,
    var cache: TtlCache<RollingFuelBurnAgreement>,
    var hits: Int,
    var misses: Int,
) : RollingFuelBurnAgreementStore {
    override fun find(code: String): RollingFuelBurnAgreement? {
        val hit = (cache.get(code) as RollingFuelBurnAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingFuelBurnAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingFuelBurnAgreement> = inner.list()
}

fun newCachedRollingFuelBurnAgreementStore(inner: RollingFuelBurnAgreementStore, ttl: Int, clock: Clock): RollingFuelBurnAgreementStore {
    return CachedRollingFuelBurnAgreementStore(
        inner = inner,
        cache = newTtlCache<RollingFuelBurnAgreement>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling fuel burn agreements. Empty fields match all. */
data class RollingFuelBurnAgreementQuery(
    var label: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingFuelBurnAgreement): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingFuelBurnAgreement>): MutableList<RollingFuelBurnAgreement> {
        val matched = filter(items) { candidate: RollingFuelBurnAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingFuelBurnAgreements(store: RollingFuelBurnAgreementStore, query: RollingFuelBurnAgreementQuery): MutableList<RollingFuelBurnAgreement> {
    return query.apply(store.list())
}
