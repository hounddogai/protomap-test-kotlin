package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge agreements by code. */
interface EvChargeAgreementStore {
    fun find(code: String): EvChargeAgreement?
    fun save(item: EvChargeAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeAgreementStore(
    var items: MutableMap<String, EvChargeAgreement>,
    var order: MutableList<String>,
) : EvChargeAgreementStore {
    override fun find(code: String): EvChargeAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeAgreement) {
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

    override fun list(): MutableList<EvChargeAgreement> {
        val out = mutableListOf<EvChargeAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeAgreementStore(): EvChargeAgreementStore {
    return MemoryEvChargeAgreementStore(mutableMapOf<String, EvChargeAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeAgreementStore(
    var inner: EvChargeAgreementStore,
    var cache: TtlCache<EvChargeAgreement>,
    var hits: Int,
    var misses: Int,
) : EvChargeAgreementStore {
    override fun find(code: String): EvChargeAgreement? {
        val hit = (cache.get(code) as EvChargeAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeAgreement> = inner.list()
}

fun newCachedEvChargeAgreementStore(inner: EvChargeAgreementStore, ttl: Int, clock: Clock): EvChargeAgreementStore {
    return CachedEvChargeAgreementStore(
        inner = inner,
        cache = newTtlCache<EvChargeAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge agreements. Empty fields match all. */
data class EvChargeAgreementQuery(
    var lane: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeAgreement): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeAgreement>): MutableList<EvChargeAgreement> {
        val matched = filter(items) { candidate: EvChargeAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeAgreements(store: EvChargeAgreementStore, query: EvChargeAgreementQuery): MutableList<EvChargeAgreement> {
    return query.apply(store.list())
}
