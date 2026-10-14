package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling emission factor agreements by code. */
interface RollingEmissionFactorAgreementStore {
    fun find(code: String): RollingEmissionFactorAgreement?
    fun save(item: RollingEmissionFactorAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingEmissionFactorAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingEmissionFactorAgreementStore(
    var items: MutableMap<String, RollingEmissionFactorAgreement>,
    var order: MutableList<String>,
) : RollingEmissionFactorAgreementStore {
    override fun find(code: String): RollingEmissionFactorAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingEmissionFactorAgreement) {
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

    override fun list(): MutableList<RollingEmissionFactorAgreement> {
        val out = mutableListOf<RollingEmissionFactorAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingEmissionFactorAgreementStore(): RollingEmissionFactorAgreementStore {
    return MemoryRollingEmissionFactorAgreementStore(
        mutableMapOf<String, RollingEmissionFactorAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingEmissionFactorAgreementStore(
    var inner: RollingEmissionFactorAgreementStore,
    var cache: TtlCache<RollingEmissionFactorAgreement>,
    var hits: Int,
    var misses: Int,
) : RollingEmissionFactorAgreementStore {
    override fun find(code: String): RollingEmissionFactorAgreement? {
        val hit = (cache.get(code) as RollingEmissionFactorAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingEmissionFactorAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingEmissionFactorAgreement> = inner.list()
}

fun newCachedRollingEmissionFactorAgreementStore(inner: RollingEmissionFactorAgreementStore, ttl: Int, clock: Clock): RollingEmissionFactorAgreementStore {
    return CachedRollingEmissionFactorAgreementStore(
        inner = inner,
        cache = newTtlCache<RollingEmissionFactorAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling emission factor agreements. Empty fields match all. */
data class RollingEmissionFactorAgreementQuery(
    var channel: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingEmissionFactorAgreement): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingEmissionFactorAgreement>): MutableList<RollingEmissionFactorAgreement> {
        val matched = filter(items) { candidate: RollingEmissionFactorAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingEmissionFactorAgreements(store: RollingEmissionFactorAgreementStore, query: RollingEmissionFactorAgreementQuery): MutableList<RollingEmissionFactorAgreement> {
    return query.apply(store.list())
}
