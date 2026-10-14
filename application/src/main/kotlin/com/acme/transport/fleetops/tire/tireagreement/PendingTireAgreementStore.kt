package com.acme.transport.fleetops.tire.tireagreement

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending tire agreements by code. */
interface PendingTireAgreementStore {
    fun find(code: String): PendingTireAgreement?
    fun save(item: PendingTireAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingTireAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingTireAgreementStore(
    var items: MutableMap<String, PendingTireAgreement>,
    var order: MutableList<String>,
) : PendingTireAgreementStore {
    override fun find(code: String): PendingTireAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingTireAgreement) {
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

    override fun list(): MutableList<PendingTireAgreement> {
        val out = mutableListOf<PendingTireAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingTireAgreementStore(): PendingTireAgreementStore {
    return MemoryPendingTireAgreementStore(
        mutableMapOf<String, PendingTireAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingTireAgreementStore(
    var inner: PendingTireAgreementStore,
    var cache: TtlCache<PendingTireAgreement>,
    var hits: Int,
    var misses: Int,
) : PendingTireAgreementStore {
    override fun find(code: String): PendingTireAgreement? {
        val hit = (cache.get(code) as PendingTireAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingTireAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingTireAgreement> = inner.list()
}

fun newCachedPendingTireAgreementStore(inner: PendingTireAgreementStore, ttl: Int, clock: Clock): PendingTireAgreementStore {
    return CachedPendingTireAgreementStore(
        inner = inner,
        cache = newTtlCache<PendingTireAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending tire agreements. Empty fields match all. */
data class PendingTireAgreementQuery(
    var lane: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingTireAgreement): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingTireAgreement>): MutableList<PendingTireAgreement> {
        val matched = filter(items) { candidate: PendingTireAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingTireAgreements(store: PendingTireAgreementStore, query: PendingTireAgreementQuery): MutableList<PendingTireAgreement> {
    return query.apply(store.list())
}
