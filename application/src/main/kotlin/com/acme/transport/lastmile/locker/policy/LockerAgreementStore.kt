package com.acme.transport.lastmile.locker.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker agreements by code. */
interface LockerAgreementStore {
    fun find(code: String): LockerAgreement?
    fun save(item: LockerAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerAgreementStore(
    var items: MutableMap<String, LockerAgreement>,
    var order: MutableList<String>,
) : LockerAgreementStore {
    override fun find(code: String): LockerAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerAgreement) {
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

    override fun list(): MutableList<LockerAgreement> {
        val out = mutableListOf<LockerAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerAgreementStore(): LockerAgreementStore {
    return MemoryLockerAgreementStore(mutableMapOf<String, LockerAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerAgreementStore(
    var inner: LockerAgreementStore,
    var cache: TtlCache<LockerAgreement>,
    var hits: Int,
    var misses: Int,
) : LockerAgreementStore {
    override fun find(code: String): LockerAgreement? {
        val hit = (cache.get(code) as LockerAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerAgreement> = inner.list()
}

fun newCachedLockerAgreementStore(inner: LockerAgreementStore, ttl: Int, clock: Clock): LockerAgreementStore {
    return CachedLockerAgreementStore(
        inner = inner,
        cache = newTtlCache<LockerAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker agreements. Empty fields match all. */
data class LockerAgreementQuery(
    var lane: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerAgreement): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerAgreement>): MutableList<LockerAgreement> {
        val matched = filter(items) { candidate: LockerAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerAgreements(store: LockerAgreementStore, query: LockerAgreementQuery): MutableList<LockerAgreement> {
    return query.apply(store.list())
}
