package com.acme.transport.network.cutoff.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff ledgers by code. */
interface CutoffLedgerStore {
    fun find(code: String): CutoffLedger?
    fun save(item: CutoffLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffLedgerStore(
    var items: MutableMap<String, CutoffLedger>,
    var order: MutableList<String>,
) : CutoffLedgerStore {
    override fun find(code: String): CutoffLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffLedger) {
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

    override fun list(): MutableList<CutoffLedger> {
        val out = mutableListOf<CutoffLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffLedgerStore(): CutoffLedgerStore {
    return MemoryCutoffLedgerStore(mutableMapOf<String, CutoffLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffLedgerStore(
    var inner: CutoffLedgerStore,
    var cache: TtlCache<CutoffLedger>,
    var hits: Int,
    var misses: Int,
) : CutoffLedgerStore {
    override fun find(code: String): CutoffLedger? {
        val hit = (cache.get(code) as CutoffLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffLedger> = inner.list()
}

fun newCachedCutoffLedgerStore(inner: CutoffLedgerStore, ttl: Int, clock: Clock): CutoffLedgerStore {
    return CachedCutoffLedgerStore(
        inner = inner,
        cache = newTtlCache<CutoffLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff ledgers. Empty fields match all. */
data class CutoffLedgerQuery(
    var name: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffLedger): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffLedger>): MutableList<CutoffLedger> {
        val matched = filter(items) { candidate: CutoffLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffLedgers(store: CutoffLedgerStore, query: CutoffLedgerQuery): MutableList<CutoffLedger> {
    return query.apply(store.list())
}
