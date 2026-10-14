package com.acme.transport.emissions.evcharge.review

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient ev charge ledgers by code. */
interface LenientEvChargeLedgerStore {
    fun find(code: String): LenientEvChargeLedger?
    fun save(item: LenientEvChargeLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientEvChargeLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientEvChargeLedgerStore(
    var items: MutableMap<String, LenientEvChargeLedger>,
    var order: MutableList<String>,
) : LenientEvChargeLedgerStore {
    override fun find(code: String): LenientEvChargeLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientEvChargeLedger) {
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

    override fun list(): MutableList<LenientEvChargeLedger> {
        val out = mutableListOf<LenientEvChargeLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientEvChargeLedgerStore(): LenientEvChargeLedgerStore {
    return MemoryLenientEvChargeLedgerStore(
        mutableMapOf<String, LenientEvChargeLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientEvChargeLedgerStore(
    var inner: LenientEvChargeLedgerStore,
    var cache: TtlCache<LenientEvChargeLedger>,
    var hits: Int,
    var misses: Int,
) : LenientEvChargeLedgerStore {
    override fun find(code: String): LenientEvChargeLedger? {
        val hit = (cache.get(code) as LenientEvChargeLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientEvChargeLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientEvChargeLedger> = inner.list()
}

fun newCachedLenientEvChargeLedgerStore(inner: LenientEvChargeLedgerStore, ttl: Int, clock: Clock): LenientEvChargeLedgerStore {
    return CachedLenientEvChargeLedgerStore(
        inner = inner,
        cache = newTtlCache<LenientEvChargeLedger>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient ev charge ledgers. Empty fields match all. */
data class LenientEvChargeLedgerQuery(
    var unit: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientEvChargeLedger): Boolean {
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

    fun apply(items: MutableList<LenientEvChargeLedger>): MutableList<LenientEvChargeLedger> {
        val matched = filter(items) { candidate: LenientEvChargeLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientEvChargeLedgers(store: LenientEvChargeLedgerStore, query: LenientEvChargeLedgerQuery): MutableList<LenientEvChargeLedger> {
    return query.apply(store.list())
}
