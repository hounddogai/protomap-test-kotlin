package com.acme.transport.lastmile.locker.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps pending locker agreements by code. */
interface PendingLockerAgreementStore {
    fun find(code: String): PendingLockerAgreement?
    fun save(item: PendingLockerAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingLockerAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingLockerAgreementStore(
    var items: MutableMap<String, PendingLockerAgreement>,
    var order: MutableList<String>,
) : PendingLockerAgreementStore {
    override fun find(code: String): PendingLockerAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingLockerAgreement) {
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

    override fun list(): MutableList<PendingLockerAgreement> {
        val out = mutableListOf<PendingLockerAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingLockerAgreementStore(): PendingLockerAgreementStore {
    return MemoryPendingLockerAgreementStore(
        mutableMapOf<String, PendingLockerAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingLockerAgreementStore(
    var inner: PendingLockerAgreementStore,
    var cache: TtlCache<PendingLockerAgreement>,
    var hits: Int,
    var misses: Int,
) : PendingLockerAgreementStore {
    override fun find(code: String): PendingLockerAgreement? {
        val hit = (cache.get(code) as PendingLockerAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingLockerAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingLockerAgreement> = inner.list()
}

fun newCachedPendingLockerAgreementStore(inner: PendingLockerAgreementStore, ttl: Int, clock: Clock): PendingLockerAgreementStore {
    return CachedPendingLockerAgreementStore(
        inner = inner,
        cache = newTtlCache<PendingLockerAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending locker agreements. Empty fields match all. */
data class PendingLockerAgreementQuery(
    var unit: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingLockerAgreement): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingLockerAgreement>): MutableList<PendingLockerAgreement> {
        val matched = filter(items) { candidate: PendingLockerAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingLockerAgreements(store: PendingLockerAgreementStore, query: PendingLockerAgreementQuery): MutableList<PendingLockerAgreement> {
    return query.apply(store.list())
}
