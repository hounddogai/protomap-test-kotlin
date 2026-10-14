package com.acme.transport.crewing.roster.recorditem

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient roster ledgers by code. */
interface LenientRosterLedgerStore {
    fun find(code: String): LenientRosterLedger?
    fun save(item: LenientRosterLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientRosterLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientRosterLedgerStore(
    var items: MutableMap<String, LenientRosterLedger>,
    var order: MutableList<String>,
) : LenientRosterLedgerStore {
    override fun find(code: String): LenientRosterLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientRosterLedger) {
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

    override fun list(): MutableList<LenientRosterLedger> {
        val out = mutableListOf<LenientRosterLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientRosterLedgerStore(): LenientRosterLedgerStore {
    return MemoryLenientRosterLedgerStore(mutableMapOf<String, LenientRosterLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientRosterLedgerStore(
    var inner: LenientRosterLedgerStore,
    var cache: TtlCache<LenientRosterLedger>,
    var hits: Int,
    var misses: Int,
) : LenientRosterLedgerStore {
    override fun find(code: String): LenientRosterLedger? {
        val hit = (cache.get(code) as LenientRosterLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientRosterLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientRosterLedger> = inner.list()
}

fun newCachedLenientRosterLedgerStore(inner: LenientRosterLedgerStore, ttl: Int, clock: Clock): LenientRosterLedgerStore {
    return CachedLenientRosterLedgerStore(
        inner = inner,
        cache = newTtlCache<LenientRosterLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient roster ledgers. Empty fields match all. */
data class LenientRosterLedgerQuery(
    var channel: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientRosterLedger): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientRosterLedger>): MutableList<LenientRosterLedger> {
        val matched = filter(items) { candidate: LenientRosterLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientRosterLedgers(store: LenientRosterLedgerStore, query: LenientRosterLedgerQuery): MutableList<LenientRosterLedger> {
    return query.apply(store.list())
}
