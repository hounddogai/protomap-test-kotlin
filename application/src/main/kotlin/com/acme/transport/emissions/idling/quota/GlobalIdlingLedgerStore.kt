package com.acme.transport.emissions.idling.quota

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global idling ledgers by code. */
interface GlobalIdlingLedgerStore {
    fun find(code: String): GlobalIdlingLedger?
    fun save(item: GlobalIdlingLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalIdlingLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalIdlingLedgerStore(
    var items: MutableMap<String, GlobalIdlingLedger>,
    var order: MutableList<String>,
) : GlobalIdlingLedgerStore {
    override fun find(code: String): GlobalIdlingLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalIdlingLedger) {
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

    override fun list(): MutableList<GlobalIdlingLedger> {
        val out = mutableListOf<GlobalIdlingLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalIdlingLedgerStore(): GlobalIdlingLedgerStore {
    return MemoryGlobalIdlingLedgerStore(mutableMapOf<String, GlobalIdlingLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalIdlingLedgerStore(
    var inner: GlobalIdlingLedgerStore,
    var cache: TtlCache<GlobalIdlingLedger>,
    var hits: Int,
    var misses: Int,
) : GlobalIdlingLedgerStore {
    override fun find(code: String): GlobalIdlingLedger? {
        val hit = (cache.get(code) as GlobalIdlingLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalIdlingLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalIdlingLedger> = inner.list()
}

fun newCachedGlobalIdlingLedgerStore(inner: GlobalIdlingLedgerStore, ttl: Int, clock: Clock): GlobalIdlingLedgerStore {
    return CachedGlobalIdlingLedgerStore(
        inner = inner,
        cache = newTtlCache<GlobalIdlingLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global idling ledgers. Empty fields match all. */
data class GlobalIdlingLedgerQuery(
    var lane: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalIdlingLedger): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalIdlingLedger>): MutableList<GlobalIdlingLedger> {
        val matched = filter(items) { candidate: GlobalIdlingLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalIdlingLedgers(store: GlobalIdlingLedgerStore, query: GlobalIdlingLedgerQuery): MutableList<GlobalIdlingLedger> {
    return query.apply(store.list())
}
