package com.acme.transport.safety.coaching.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary coaching ledgers by code. */
interface PrimaryCoachingLedgerStore {
    fun find(code: String): PrimaryCoachingLedger?
    fun save(item: PrimaryCoachingLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCoachingLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCoachingLedgerStore(
    var items: MutableMap<String, PrimaryCoachingLedger>,
    var order: MutableList<String>,
) : PrimaryCoachingLedgerStore {
    override fun find(code: String): PrimaryCoachingLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCoachingLedger) {
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

    override fun list(): MutableList<PrimaryCoachingLedger> {
        val out = mutableListOf<PrimaryCoachingLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCoachingLedgerStore(): PrimaryCoachingLedgerStore {
    return MemoryPrimaryCoachingLedgerStore(
        mutableMapOf<String, PrimaryCoachingLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCoachingLedgerStore(
    var inner: PrimaryCoachingLedgerStore,
    var cache: TtlCache<PrimaryCoachingLedger>,
    var hits: Int,
    var misses: Int,
) : PrimaryCoachingLedgerStore {
    override fun find(code: String): PrimaryCoachingLedger? {
        val hit = (cache.get(code) as PrimaryCoachingLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCoachingLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCoachingLedger> = inner.list()
}

fun newCachedPrimaryCoachingLedgerStore(inner: PrimaryCoachingLedgerStore, ttl: Int, clock: Clock): PrimaryCoachingLedgerStore {
    return CachedPrimaryCoachingLedgerStore(
        inner = inner,
        cache = newTtlCache<PrimaryCoachingLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary coaching ledgers. Empty fields match all. */
data class PrimaryCoachingLedgerQuery(
    var title: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCoachingLedger): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryCoachingLedger>): MutableList<PrimaryCoachingLedger> {
        val matched = filter(items) { candidate: PrimaryCoachingLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCoachingLedgers(store: PrimaryCoachingLedgerStore, query: PrimaryCoachingLedgerQuery): MutableList<PrimaryCoachingLedger> {
    return query.apply(store.list())
}
