package com.acme.transport.crewing.roster.forecast

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster ledgers by code. */
interface RosterLedgerStore {
    fun find(code: String): RosterLedger?
    fun save(item: RosterLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterLedgerStore(
    var items: MutableMap<String, RosterLedger>,
    var order: MutableList<String>,
) : RosterLedgerStore {
    override fun find(code: String): RosterLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterLedger) {
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

    override fun list(): MutableList<RosterLedger> {
        val out = mutableListOf<RosterLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterLedgerStore(): RosterLedgerStore {
    return MemoryRosterLedgerStore(mutableMapOf<String, RosterLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterLedgerStore(
    var inner: RosterLedgerStore,
    var cache: TtlCache<RosterLedger>,
    var hits: Int,
    var misses: Int,
) : RosterLedgerStore {
    override fun find(code: String): RosterLedger? {
        val hit = (cache.get(code) as RosterLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterLedger> = inner.list()
}

fun newCachedRosterLedgerStore(inner: RosterLedgerStore, ttl: Int, clock: Clock): RosterLedgerStore {
    return CachedRosterLedgerStore(
        inner = inner,
        cache = newTtlCache<RosterLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster ledgers. Empty fields match all. */
data class RosterLedgerQuery(
    var category: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterLedger): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterLedger>): MutableList<RosterLedger> {
        val matched = filter(items) { candidate: RosterLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterLedgers(store: RosterLedgerStore, query: RosterLedgerQuery): MutableList<RosterLedger> {
    return query.apply(store.list())
}
