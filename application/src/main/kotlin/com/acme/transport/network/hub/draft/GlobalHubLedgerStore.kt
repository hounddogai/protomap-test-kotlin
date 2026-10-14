package com.acme.transport.network.hub.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps global hub ledgers by code. */
interface GlobalHubLedgerStore {
    fun find(code: String): GlobalHubLedger?
    fun save(item: GlobalHubLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalHubLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalHubLedgerStore(
    var items: MutableMap<String, GlobalHubLedger>,
    var order: MutableList<String>,
) : GlobalHubLedgerStore {
    override fun find(code: String): GlobalHubLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalHubLedger) {
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

    override fun list(): MutableList<GlobalHubLedger> {
        val out = mutableListOf<GlobalHubLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalHubLedgerStore(): GlobalHubLedgerStore {
    return MemoryGlobalHubLedgerStore(mutableMapOf<String, GlobalHubLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalHubLedgerStore(
    var inner: GlobalHubLedgerStore,
    var cache: TtlCache<GlobalHubLedger>,
    var hits: Int,
    var misses: Int,
) : GlobalHubLedgerStore {
    override fun find(code: String): GlobalHubLedger? {
        val hit = (cache.get(code) as GlobalHubLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalHubLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalHubLedger> = inner.list()
}

fun newCachedGlobalHubLedgerStore(inner: GlobalHubLedgerStore, ttl: Int, clock: Clock): GlobalHubLedgerStore {
    return CachedGlobalHubLedgerStore(
        inner = inner,
        cache = newTtlCache<GlobalHubLedger>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global hub ledgers. Empty fields match all. */
data class GlobalHubLedgerQuery(
    var country: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalHubLedger): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<GlobalHubLedger>): MutableList<GlobalHubLedger> {
        val matched = filter(items) { candidate: GlobalHubLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalHubLedgers(store: GlobalHubLedgerStore, query: GlobalHubLedgerQuery): MutableList<GlobalHubLedger> {
    return query.apply(store.list())
}
