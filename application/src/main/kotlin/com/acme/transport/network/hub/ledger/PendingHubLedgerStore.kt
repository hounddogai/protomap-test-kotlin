package com.acme.transport.network.hub.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps pending hub ledgers by code. */
interface PendingHubLedgerStore {
    fun find(code: String): PendingHubLedger?
    fun save(item: PendingHubLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingHubLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingHubLedgerStore(
    var items: MutableMap<String, PendingHubLedger>,
    var order: MutableList<String>,
) : PendingHubLedgerStore {
    override fun find(code: String): PendingHubLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingHubLedger) {
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

    override fun list(): MutableList<PendingHubLedger> {
        val out = mutableListOf<PendingHubLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingHubLedgerStore(): PendingHubLedgerStore {
    return MemoryPendingHubLedgerStore(mutableMapOf<String, PendingHubLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingHubLedgerStore(
    var inner: PendingHubLedgerStore,
    var cache: TtlCache<PendingHubLedger>,
    var hits: Int,
    var misses: Int,
) : PendingHubLedgerStore {
    override fun find(code: String): PendingHubLedger? {
        val hit = (cache.get(code) as PendingHubLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingHubLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingHubLedger> = inner.list()
}

fun newCachedPendingHubLedgerStore(inner: PendingHubLedgerStore, ttl: Int, clock: Clock): PendingHubLedgerStore {
    return CachedPendingHubLedgerStore(
        inner = inner,
        cache = newTtlCache<PendingHubLedger>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending hub ledgers. Empty fields match all. */
data class PendingHubLedgerQuery(
    var locale: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingHubLedger): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingHubLedger>): MutableList<PendingHubLedger> {
        val matched = filter(items) { candidate: PendingHubLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingHubLedgers(store: PendingHubLedgerStore, query: PendingHubLedgerQuery): MutableList<PendingHubLedger> {
    return query.apply(store.list())
}
