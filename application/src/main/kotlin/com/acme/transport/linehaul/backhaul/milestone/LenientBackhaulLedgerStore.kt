package com.acme.transport.linehaul.backhaul.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lenient backhaul ledgers by code. */
interface LenientBackhaulLedgerStore {
    fun find(code: String): LenientBackhaulLedger?
    fun save(item: LenientBackhaulLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientBackhaulLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientBackhaulLedgerStore(
    var items: MutableMap<String, LenientBackhaulLedger>,
    var order: MutableList<String>,
) : LenientBackhaulLedgerStore {
    override fun find(code: String): LenientBackhaulLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientBackhaulLedger) {
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

    override fun list(): MutableList<LenientBackhaulLedger> {
        val out = mutableListOf<LenientBackhaulLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientBackhaulLedgerStore(): LenientBackhaulLedgerStore {
    return MemoryLenientBackhaulLedgerStore(
        mutableMapOf<String, LenientBackhaulLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientBackhaulLedgerStore(
    var inner: LenientBackhaulLedgerStore,
    var cache: TtlCache<LenientBackhaulLedger>,
    var hits: Int,
    var misses: Int,
) : LenientBackhaulLedgerStore {
    override fun find(code: String): LenientBackhaulLedger? {
        val hit = (cache.get(code) as LenientBackhaulLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientBackhaulLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientBackhaulLedger> = inner.list()
}

fun newCachedLenientBackhaulLedgerStore(inner: LenientBackhaulLedgerStore, ttl: Int, clock: Clock): LenientBackhaulLedgerStore {
    return CachedLenientBackhaulLedgerStore(
        inner = inner,
        cache = newTtlCache<LenientBackhaulLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient backhaul ledgers. Empty fields match all. */
data class LenientBackhaulLedgerQuery(
    var unit: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientBackhaulLedger): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientBackhaulLedger>): MutableList<LenientBackhaulLedger> {
        val matched = filter(items) { candidate: LenientBackhaulLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientBackhaulLedgers(store: LenientBackhaulLedgerStore, query: LenientBackhaulLedgerQuery): MutableList<LenientBackhaulLedger> {
    return query.apply(store.list())
}
