package com.acme.transport.lastmile.drop.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps adaptive drop ledgers by code. */
interface AdaptiveDropLedgerStore {
    fun find(code: String): AdaptiveDropLedger?
    fun save(item: AdaptiveDropLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDropLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDropLedgerStore(
    var items: MutableMap<String, AdaptiveDropLedger>,
    var order: MutableList<String>,
) : AdaptiveDropLedgerStore {
    override fun find(code: String): AdaptiveDropLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDropLedger) {
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

    override fun list(): MutableList<AdaptiveDropLedger> {
        val out = mutableListOf<AdaptiveDropLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDropLedgerStore(): AdaptiveDropLedgerStore {
    return MemoryAdaptiveDropLedgerStore(mutableMapOf<String, AdaptiveDropLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDropLedgerStore(
    var inner: AdaptiveDropLedgerStore,
    var cache: TtlCache<AdaptiveDropLedger>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDropLedgerStore {
    override fun find(code: String): AdaptiveDropLedger? {
        val hit = (cache.get(code) as AdaptiveDropLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDropLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDropLedger> = inner.list()
}

fun newCachedAdaptiveDropLedgerStore(inner: AdaptiveDropLedgerStore, ttl: Int, clock: Clock): AdaptiveDropLedgerStore {
    return CachedAdaptiveDropLedgerStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDropLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive drop ledgers. Empty fields match all. */
data class AdaptiveDropLedgerQuery(
    var title: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDropLedger): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<AdaptiveDropLedger>): MutableList<AdaptiveDropLedger> {
        val matched = filter(items) { candidate: AdaptiveDropLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDropLedgers(store: AdaptiveDropLedgerStore, query: AdaptiveDropLedgerQuery): MutableList<AdaptiveDropLedger> {
    return query.apply(store.list())
}
