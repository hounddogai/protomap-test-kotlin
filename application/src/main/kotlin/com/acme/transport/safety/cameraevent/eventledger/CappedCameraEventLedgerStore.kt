package com.acme.transport.safety.cameraevent.eventledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps capped camera event ledgers by code. */
interface CappedCameraEventLedgerStore {
    fun find(code: String): CappedCameraEventLedger?
    fun save(item: CappedCameraEventLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedCameraEventLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedCameraEventLedgerStore(
    var items: MutableMap<String, CappedCameraEventLedger>,
    var order: MutableList<String>,
) : CappedCameraEventLedgerStore {
    override fun find(code: String): CappedCameraEventLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedCameraEventLedger) {
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

    override fun list(): MutableList<CappedCameraEventLedger> {
        val out = mutableListOf<CappedCameraEventLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedCameraEventLedgerStore(): CappedCameraEventLedgerStore {
    return MemoryCappedCameraEventLedgerStore(
        mutableMapOf<String, CappedCameraEventLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedCameraEventLedgerStore(
    var inner: CappedCameraEventLedgerStore,
    var cache: TtlCache<CappedCameraEventLedger>,
    var hits: Int,
    var misses: Int,
) : CappedCameraEventLedgerStore {
    override fun find(code: String): CappedCameraEventLedger? {
        val hit = (cache.get(code) as CappedCameraEventLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedCameraEventLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedCameraEventLedger> = inner.list()
}

fun newCachedCappedCameraEventLedgerStore(inner: CappedCameraEventLedgerStore, ttl: Int, clock: Clock): CappedCameraEventLedgerStore {
    return CachedCappedCameraEventLedgerStore(
        inner = inner,
        cache = newTtlCache<CappedCameraEventLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped camera event ledgers. Empty fields match all. */
data class CappedCameraEventLedgerQuery(
    var name: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedCameraEventLedger): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<CappedCameraEventLedger>): MutableList<CappedCameraEventLedger> {
        val matched = filter(items) { candidate: CappedCameraEventLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedCameraEventLedgers(store: CappedCameraEventLedgerStore, query: CappedCameraEventLedgerQuery): MutableList<CappedCameraEventLedger> {
    return query.apply(store.list())
}
