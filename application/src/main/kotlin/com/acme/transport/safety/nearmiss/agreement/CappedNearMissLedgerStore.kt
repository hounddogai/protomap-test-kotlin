package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps capped near miss ledgers by code. */
interface CappedNearMissLedgerStore {
    fun find(code: String): CappedNearMissLedger?
    fun save(item: CappedNearMissLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedNearMissLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedNearMissLedgerStore(
    var items: MutableMap<String, CappedNearMissLedger>,
    var order: MutableList<String>,
) : CappedNearMissLedgerStore {
    override fun find(code: String): CappedNearMissLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedNearMissLedger) {
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

    override fun list(): MutableList<CappedNearMissLedger> {
        val out = mutableListOf<CappedNearMissLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedNearMissLedgerStore(): CappedNearMissLedgerStore {
    return MemoryCappedNearMissLedgerStore(
        mutableMapOf<String, CappedNearMissLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedNearMissLedgerStore(
    var inner: CappedNearMissLedgerStore,
    var cache: TtlCache<CappedNearMissLedger>,
    var hits: Int,
    var misses: Int,
) : CappedNearMissLedgerStore {
    override fun find(code: String): CappedNearMissLedger? {
        val hit = (cache.get(code) as CappedNearMissLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedNearMissLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedNearMissLedger> = inner.list()
}

fun newCachedCappedNearMissLedgerStore(inner: CappedNearMissLedgerStore, ttl: Int, clock: Clock): CappedNearMissLedgerStore {
    return CachedCappedNearMissLedgerStore(
        inner = inner,
        cache = newTtlCache<CappedNearMissLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped near miss ledgers. Empty fields match all. */
data class CappedNearMissLedgerQuery(
    var lane: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedNearMissLedger): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedNearMissLedger>): MutableList<CappedNearMissLedger> {
        val matched = filter(items) { candidate: CappedNearMissLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedNearMissLedgers(store: CappedNearMissLedgerStore, query: CappedNearMissLedgerQuery): MutableList<CappedNearMissLedger> {
    return query.apply(store.list())
}
