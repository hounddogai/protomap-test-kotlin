package com.acme.transport.network.cutoff.cutoffledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default cutoff ledgers by code. */
interface DefaultCutoffLedgerStore {
    fun find(code: String): DefaultCutoffLedger?
    fun save(item: DefaultCutoffLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCutoffLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCutoffLedgerStore(
    var items: MutableMap<String, DefaultCutoffLedger>,
    var order: MutableList<String>,
) : DefaultCutoffLedgerStore {
    override fun find(code: String): DefaultCutoffLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCutoffLedger) {
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

    override fun list(): MutableList<DefaultCutoffLedger> {
        val out = mutableListOf<DefaultCutoffLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCutoffLedgerStore(): DefaultCutoffLedgerStore {
    return MemoryDefaultCutoffLedgerStore(mutableMapOf<String, DefaultCutoffLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCutoffLedgerStore(
    var inner: DefaultCutoffLedgerStore,
    var cache: TtlCache<DefaultCutoffLedger>,
    var hits: Int,
    var misses: Int,
) : DefaultCutoffLedgerStore {
    override fun find(code: String): DefaultCutoffLedger? {
        val hit = (cache.get(code) as DefaultCutoffLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCutoffLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCutoffLedger> = inner.list()
}

fun newCachedDefaultCutoffLedgerStore(inner: DefaultCutoffLedgerStore, ttl: Int, clock: Clock): DefaultCutoffLedgerStore {
    return CachedDefaultCutoffLedgerStore(
        inner = inner,
        cache = newTtlCache<DefaultCutoffLedger>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default cutoff ledgers. Empty fields match all. */
data class DefaultCutoffLedgerQuery(
    var lane: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCutoffLedger): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultCutoffLedger>): MutableList<DefaultCutoffLedger> {
        val matched = filter(items) { candidate: DefaultCutoffLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCutoffLedgers(store: DefaultCutoffLedgerStore, query: DefaultCutoffLedgerQuery): MutableList<DefaultCutoffLedger> {
    return query.apply(store.list())
}
