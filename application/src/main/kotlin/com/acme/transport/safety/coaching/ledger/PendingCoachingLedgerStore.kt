package com.acme.transport.safety.coaching.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps pending coaching ledgers by code. */
interface PendingCoachingLedgerStore {
    fun find(code: String): PendingCoachingLedger?
    fun save(item: PendingCoachingLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingCoachingLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingCoachingLedgerStore(
    var items: MutableMap<String, PendingCoachingLedger>,
    var order: MutableList<String>,
) : PendingCoachingLedgerStore {
    override fun find(code: String): PendingCoachingLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingCoachingLedger) {
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

    override fun list(): MutableList<PendingCoachingLedger> {
        val out = mutableListOf<PendingCoachingLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingCoachingLedgerStore(): PendingCoachingLedgerStore {
    return MemoryPendingCoachingLedgerStore(
        mutableMapOf<String, PendingCoachingLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingCoachingLedgerStore(
    var inner: PendingCoachingLedgerStore,
    var cache: TtlCache<PendingCoachingLedger>,
    var hits: Int,
    var misses: Int,
) : PendingCoachingLedgerStore {
    override fun find(code: String): PendingCoachingLedger? {
        val hit = (cache.get(code) as PendingCoachingLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingCoachingLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingCoachingLedger> = inner.list()
}

fun newCachedPendingCoachingLedgerStore(inner: PendingCoachingLedgerStore, ttl: Int, clock: Clock): PendingCoachingLedgerStore {
    return CachedPendingCoachingLedgerStore(
        inner = inner,
        cache = newTtlCache<PendingCoachingLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending coaching ledgers. Empty fields match all. */
data class PendingCoachingLedgerQuery(
    var unit: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingCoachingLedger): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingCoachingLedger>): MutableList<PendingCoachingLedger> {
        val matched = filter(items) { candidate: PendingCoachingLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingCoachingLedgers(store: PendingCoachingLedgerStore, query: PendingCoachingLedgerQuery): MutableList<PendingCoachingLedger> {
    return query.apply(store.list())
}
