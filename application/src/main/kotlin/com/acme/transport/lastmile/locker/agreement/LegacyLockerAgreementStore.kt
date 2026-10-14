package com.acme.transport.lastmile.locker.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps legacy locker agreements by code. */
interface LegacyLockerAgreementStore {
    fun find(code: String): LegacyLockerAgreement?
    fun save(item: LegacyLockerAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyLockerAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyLockerAgreementStore(
    var items: MutableMap<String, LegacyLockerAgreement>,
    var order: MutableList<String>,
) : LegacyLockerAgreementStore {
    override fun find(code: String): LegacyLockerAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyLockerAgreement) {
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

    override fun list(): MutableList<LegacyLockerAgreement> {
        val out = mutableListOf<LegacyLockerAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyLockerAgreementStore(): LegacyLockerAgreementStore {
    return MemoryLegacyLockerAgreementStore(
        mutableMapOf<String, LegacyLockerAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyLockerAgreementStore(
    var inner: LegacyLockerAgreementStore,
    var cache: TtlCache<LegacyLockerAgreement>,
    var hits: Int,
    var misses: Int,
) : LegacyLockerAgreementStore {
    override fun find(code: String): LegacyLockerAgreement? {
        val hit = (cache.get(code) as LegacyLockerAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyLockerAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyLockerAgreement> = inner.list()
}

fun newCachedLegacyLockerAgreementStore(inner: LegacyLockerAgreementStore, ttl: Int, clock: Clock): LegacyLockerAgreementStore {
    return CachedLegacyLockerAgreementStore(
        inner = inner,
        cache = newTtlCache<LegacyLockerAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy locker agreements. Empty fields match all. */
data class LegacyLockerAgreementQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyLockerAgreement): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyLockerAgreement>): MutableList<LegacyLockerAgreement> {
        val matched = filter(items) { candidate: LegacyLockerAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyLockerAgreements(store: LegacyLockerAgreementStore, query: LegacyLockerAgreementQuery): MutableList<LegacyLockerAgreement> {
    return query.apply(store.list())
}
