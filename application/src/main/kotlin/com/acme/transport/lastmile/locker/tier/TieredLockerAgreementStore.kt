package com.acme.transport.lastmile.locker.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps tiered locker agreements by code. */
interface TieredLockerAgreementStore {
    fun find(code: String): TieredLockerAgreement?
    fun save(item: TieredLockerAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredLockerAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredLockerAgreementStore(
    var items: MutableMap<String, TieredLockerAgreement>,
    var order: MutableList<String>,
) : TieredLockerAgreementStore {
    override fun find(code: String): TieredLockerAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredLockerAgreement) {
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

    override fun list(): MutableList<TieredLockerAgreement> {
        val out = mutableListOf<TieredLockerAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredLockerAgreementStore(): TieredLockerAgreementStore {
    return MemoryTieredLockerAgreementStore(
        mutableMapOf<String, TieredLockerAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredLockerAgreementStore(
    var inner: TieredLockerAgreementStore,
    var cache: TtlCache<TieredLockerAgreement>,
    var hits: Int,
    var misses: Int,
) : TieredLockerAgreementStore {
    override fun find(code: String): TieredLockerAgreement? {
        val hit = (cache.get(code) as TieredLockerAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredLockerAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredLockerAgreement> = inner.list()
}

fun newCachedTieredLockerAgreementStore(inner: TieredLockerAgreementStore, ttl: Int, clock: Clock): TieredLockerAgreementStore {
    return CachedTieredLockerAgreementStore(
        inner = inner,
        cache = newTtlCache<TieredLockerAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered locker agreements. Empty fields match all. */
data class TieredLockerAgreementQuery(
    var region: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredLockerAgreement): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredLockerAgreement>): MutableList<TieredLockerAgreement> {
        val matched = filter(items) { candidate: TieredLockerAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredLockerAgreements(store: TieredLockerAgreementStore, query: TieredLockerAgreementQuery): MutableList<TieredLockerAgreement> {
    return query.apply(store.list())
}
