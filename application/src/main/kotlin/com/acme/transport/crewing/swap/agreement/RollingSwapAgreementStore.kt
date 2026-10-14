package com.acme.transport.crewing.swap.agreement

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling swap agreements by code. */
interface RollingSwapAgreementStore {
    fun find(code: String): RollingSwapAgreement?
    fun save(item: RollingSwapAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingSwapAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingSwapAgreementStore(
    var items: MutableMap<String, RollingSwapAgreement>,
    var order: MutableList<String>,
) : RollingSwapAgreementStore {
    override fun find(code: String): RollingSwapAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingSwapAgreement) {
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

    override fun list(): MutableList<RollingSwapAgreement> {
        val out = mutableListOf<RollingSwapAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingSwapAgreementStore(): RollingSwapAgreementStore {
    return MemoryRollingSwapAgreementStore(
        mutableMapOf<String, RollingSwapAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingSwapAgreementStore(
    var inner: RollingSwapAgreementStore,
    var cache: TtlCache<RollingSwapAgreement>,
    var hits: Int,
    var misses: Int,
) : RollingSwapAgreementStore {
    override fun find(code: String): RollingSwapAgreement? {
        val hit = (cache.get(code) as RollingSwapAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingSwapAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingSwapAgreement> = inner.list()
}

fun newCachedRollingSwapAgreementStore(inner: RollingSwapAgreementStore, ttl: Int, clock: Clock): RollingSwapAgreementStore {
    return CachedRollingSwapAgreementStore(
        inner = inner,
        cache = newTtlCache<RollingSwapAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling swap agreements. Empty fields match all. */
data class RollingSwapAgreementQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingSwapAgreement): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingSwapAgreement>): MutableList<RollingSwapAgreement> {
        val matched = filter(items) { candidate: RollingSwapAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingSwapAgreements(store: RollingSwapAgreementStore, query: RollingSwapAgreementQuery): MutableList<RollingSwapAgreement> {
    return query.apply(store.list())
}
