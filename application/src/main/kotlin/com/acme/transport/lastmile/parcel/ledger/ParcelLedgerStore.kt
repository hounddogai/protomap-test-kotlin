package com.acme.transport.lastmile.parcel.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel ledgers by code. */
interface ParcelLedgerStore {
    fun find(code: String): ParcelLedger?
    fun save(item: ParcelLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelLedgerStore(
    var items: MutableMap<String, ParcelLedger>,
    var order: MutableList<String>,
) : ParcelLedgerStore {
    override fun find(code: String): ParcelLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelLedger) {
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

    override fun list(): MutableList<ParcelLedger> {
        val out = mutableListOf<ParcelLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelLedgerStore(): ParcelLedgerStore {
    return MemoryParcelLedgerStore(mutableMapOf<String, ParcelLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelLedgerStore(
    var inner: ParcelLedgerStore,
    var cache: TtlCache<ParcelLedger>,
    var hits: Int,
    var misses: Int,
) : ParcelLedgerStore {
    override fun find(code: String): ParcelLedger? {
        val hit = (cache.get(code) as ParcelLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelLedger> = inner.list()
}

fun newCachedParcelLedgerStore(inner: ParcelLedgerStore, ttl: Int, clock: Clock): ParcelLedgerStore {
    return CachedParcelLedgerStore(
        inner = inner,
        cache = newTtlCache<ParcelLedger>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel ledgers. Empty fields match all. */
data class ParcelLedgerQuery(
    var depot: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelLedger): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelLedger>): MutableList<ParcelLedger> {
        val matched = filter(items) { candidate: ParcelLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelLedgers(store: ParcelLedgerStore, query: ParcelLedgerQuery): MutableList<ParcelLedger> {
    return query.apply(store.list())
}
