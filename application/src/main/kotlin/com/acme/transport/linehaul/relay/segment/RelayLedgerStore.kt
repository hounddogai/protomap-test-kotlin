package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay ledgers by code. */
interface RelayLedgerStore {
    fun find(code: String): RelayLedger?
    fun save(item: RelayLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayLedgerStore(
    var items: MutableMap<String, RelayLedger>,
    var order: MutableList<String>,
) : RelayLedgerStore {
    override fun find(code: String): RelayLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayLedger) {
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

    override fun list(): MutableList<RelayLedger> {
        val out = mutableListOf<RelayLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayLedgerStore(): RelayLedgerStore {
    return MemoryRelayLedgerStore(mutableMapOf<String, RelayLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayLedgerStore(
    var inner: RelayLedgerStore,
    var cache: TtlCache<RelayLedger>,
    var hits: Int,
    var misses: Int,
) : RelayLedgerStore {
    override fun find(code: String): RelayLedger? {
        val hit = (cache.get(code) as RelayLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayLedger> = inner.list()
}

fun newCachedRelayLedgerStore(inner: RelayLedgerStore, ttl: Int, clock: Clock): RelayLedgerStore {
    return CachedRelayLedgerStore(
        inner = inner,
        cache = newTtlCache<RelayLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay ledgers. Empty fields match all. */
data class RelayLedgerQuery(
    var category: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayLedger): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayLedger>): MutableList<RelayLedger> {
        val matched = filter(items) { candidate: RelayLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayLedgers(store: RelayLedgerStore, query: RelayLedgerQuery): MutableList<RelayLedger> {
    return query.apply(store.list())
}
