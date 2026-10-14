package com.acme.transport.lastmile.locker.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled locker agreements by code. */
interface ScheduledLockerAgreementStore {
    fun find(code: String): ScheduledLockerAgreement?
    fun save(item: ScheduledLockerAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledLockerAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledLockerAgreementStore(
    var items: MutableMap<String, ScheduledLockerAgreement>,
    var order: MutableList<String>,
) : ScheduledLockerAgreementStore {
    override fun find(code: String): ScheduledLockerAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledLockerAgreement) {
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

    override fun list(): MutableList<ScheduledLockerAgreement> {
        val out = mutableListOf<ScheduledLockerAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledLockerAgreementStore(): ScheduledLockerAgreementStore {
    return MemoryScheduledLockerAgreementStore(
        mutableMapOf<String, ScheduledLockerAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledLockerAgreementStore(
    var inner: ScheduledLockerAgreementStore,
    var cache: TtlCache<ScheduledLockerAgreement>,
    var hits: Int,
    var misses: Int,
) : ScheduledLockerAgreementStore {
    override fun find(code: String): ScheduledLockerAgreement? {
        val hit = (cache.get(code) as ScheduledLockerAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledLockerAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledLockerAgreement> = inner.list()
}

fun newCachedScheduledLockerAgreementStore(inner: ScheduledLockerAgreementStore, ttl: Int, clock: Clock): ScheduledLockerAgreementStore {
    return CachedScheduledLockerAgreementStore(
        inner = inner,
        cache = newTtlCache<ScheduledLockerAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled locker agreements. Empty fields match all. */
data class ScheduledLockerAgreementQuery(
    var country: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledLockerAgreement): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledLockerAgreement>): MutableList<ScheduledLockerAgreement> {
        val matched = filter(items) { candidate: ScheduledLockerAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledLockerAgreements(store: ScheduledLockerAgreementStore, query: ScheduledLockerAgreementQuery): MutableList<ScheduledLockerAgreement> {
    return query.apply(store.list())
}
