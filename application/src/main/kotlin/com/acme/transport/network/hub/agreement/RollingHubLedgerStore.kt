package com.acme.transport.network.hub.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps rolling hub ledgers by code. */
interface RollingHubLedgerStore {
    fun find(code: String): RollingHubLedger?
    fun save(item: RollingHubLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingHubLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingHubLedgerStore(
    var items: MutableMap<String, RollingHubLedger>,
    var order: MutableList<String>,
) : RollingHubLedgerStore {
    override fun find(code: String): RollingHubLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingHubLedger) {
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

    override fun list(): MutableList<RollingHubLedger> {
        val out = mutableListOf<RollingHubLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingHubLedgerStore(): RollingHubLedgerStore {
    return MemoryRollingHubLedgerStore(mutableMapOf<String, RollingHubLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingHubLedgerStore(
    var inner: RollingHubLedgerStore,
    var cache: TtlCache<RollingHubLedger>,
    var hits: Int,
    var misses: Int,
) : RollingHubLedgerStore {
    override fun find(code: String): RollingHubLedger? {
        val hit = (cache.get(code) as RollingHubLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingHubLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingHubLedger> = inner.list()
}

fun newCachedRollingHubLedgerStore(inner: RollingHubLedgerStore, ttl: Int, clock: Clock): RollingHubLedgerStore {
    return CachedRollingHubLedgerStore(
        inner = inner,
        cache = newTtlCache<RollingHubLedger>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling hub ledgers. Empty fields match all. */
data class RollingHubLedgerQuery(
    var country: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingHubLedger): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingHubLedger>): MutableList<RollingHubLedger> {
        val matched = filter(items) { candidate: RollingHubLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingHubLedgers(store: RollingHubLedgerStore, query: RollingHubLedgerQuery): MutableList<RollingHubLedger> {
    return query.apply(store.list())
}
