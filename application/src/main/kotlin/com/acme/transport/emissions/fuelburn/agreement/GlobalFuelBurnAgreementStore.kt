package com.acme.transport.emissions.fuelburn.agreement

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global fuel burn agreements by code. */
interface GlobalFuelBurnAgreementStore {
    fun find(code: String): GlobalFuelBurnAgreement?
    fun save(item: GlobalFuelBurnAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalFuelBurnAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalFuelBurnAgreementStore(
    var items: MutableMap<String, GlobalFuelBurnAgreement>,
    var order: MutableList<String>,
) : GlobalFuelBurnAgreementStore {
    override fun find(code: String): GlobalFuelBurnAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalFuelBurnAgreement) {
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

    override fun list(): MutableList<GlobalFuelBurnAgreement> {
        val out = mutableListOf<GlobalFuelBurnAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalFuelBurnAgreementStore(): GlobalFuelBurnAgreementStore {
    return MemoryGlobalFuelBurnAgreementStore(
        mutableMapOf<String, GlobalFuelBurnAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalFuelBurnAgreementStore(
    var inner: GlobalFuelBurnAgreementStore,
    var cache: TtlCache<GlobalFuelBurnAgreement>,
    var hits: Int,
    var misses: Int,
) : GlobalFuelBurnAgreementStore {
    override fun find(code: String): GlobalFuelBurnAgreement? {
        val hit = (cache.get(code) as GlobalFuelBurnAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalFuelBurnAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalFuelBurnAgreement> = inner.list()
}

fun newCachedGlobalFuelBurnAgreementStore(inner: GlobalFuelBurnAgreementStore, ttl: Int, clock: Clock): GlobalFuelBurnAgreementStore {
    return CachedGlobalFuelBurnAgreementStore(
        inner = inner,
        cache = newTtlCache<GlobalFuelBurnAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global fuel burn agreements. Empty fields match all. */
data class GlobalFuelBurnAgreementQuery(
    var label: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalFuelBurnAgreement): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalFuelBurnAgreement>): MutableList<GlobalFuelBurnAgreement> {
        val matched = filter(items) { candidate: GlobalFuelBurnAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalFuelBurnAgreements(store: GlobalFuelBurnAgreementStore, query: GlobalFuelBurnAgreementQuery): MutableList<GlobalFuelBurnAgreement> {
    return query.apply(store.list())
}
