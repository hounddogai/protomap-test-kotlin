package com.acme.transport.network.hub.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub ledgers by code. */
interface HubLedgerStore {
    fun find(code: String): HubLedger?
    fun save(item: HubLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubLedgerStore(
    var items: MutableMap<String, HubLedger>,
    var order: MutableList<String>,
) : HubLedgerStore {
    override fun find(code: String): HubLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubLedger) {
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

    override fun list(): MutableList<HubLedger> {
        val out = mutableListOf<HubLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubLedgerStore(): HubLedgerStore {
    return MemoryHubLedgerStore(mutableMapOf<String, HubLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubLedgerStore(
    var inner: HubLedgerStore,
    var cache: TtlCache<HubLedger>,
    var hits: Int,
    var misses: Int,
) : HubLedgerStore {
    override fun find(code: String): HubLedger? {
        val hit = (cache.get(code) as HubLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubLedger> = inner.list()
}

fun newCachedHubLedgerStore(inner: HubLedgerStore, ttl: Int, clock: Clock): HubLedgerStore {
    return CachedHubLedgerStore(
        inner = inner,
        cache = newTtlCache<HubLedger>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub ledgers. Empty fields match all. */
data class HubLedgerQuery(
    var channel: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubLedger): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubLedger>): MutableList<HubLedger> {
        val matched = filter(items) { candidate: HubLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubLedgers(store: HubLedgerStore, query: HubLedgerQuery): MutableList<HubLedger> {
    return query.apply(store.list())
}
