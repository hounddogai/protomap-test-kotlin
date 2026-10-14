package com.acme.transport.fleetops.serviceplan.proposal

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling service plan ledgers by code. */
interface RollingServicePlanLedgerStore {
    fun find(code: String): RollingServicePlanLedger?
    fun save(item: RollingServicePlanLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingServicePlanLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingServicePlanLedgerStore(
    var items: MutableMap<String, RollingServicePlanLedger>,
    var order: MutableList<String>,
) : RollingServicePlanLedgerStore {
    override fun find(code: String): RollingServicePlanLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingServicePlanLedger) {
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

    override fun list(): MutableList<RollingServicePlanLedger> {
        val out = mutableListOf<RollingServicePlanLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingServicePlanLedgerStore(): RollingServicePlanLedgerStore {
    return MemoryRollingServicePlanLedgerStore(
        mutableMapOf<String, RollingServicePlanLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingServicePlanLedgerStore(
    var inner: RollingServicePlanLedgerStore,
    var cache: TtlCache<RollingServicePlanLedger>,
    var hits: Int,
    var misses: Int,
) : RollingServicePlanLedgerStore {
    override fun find(code: String): RollingServicePlanLedger? {
        val hit = (cache.get(code) as RollingServicePlanLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingServicePlanLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingServicePlanLedger> = inner.list()
}

fun newCachedRollingServicePlanLedgerStore(inner: RollingServicePlanLedgerStore, ttl: Int, clock: Clock): RollingServicePlanLedgerStore {
    return CachedRollingServicePlanLedgerStore(
        inner = inner,
        cache = newTtlCache<RollingServicePlanLedger>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling service plan ledgers. Empty fields match all. */
data class RollingServicePlanLedgerQuery(
    var name: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingServicePlanLedger): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingServicePlanLedger>): MutableList<RollingServicePlanLedger> {
        val matched = filter(items) { candidate: RollingServicePlanLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingServicePlanLedgers(store: RollingServicePlanLedgerStore, query: RollingServicePlanLedgerQuery): MutableList<RollingServicePlanLedger> {
    return query.apply(store.list())
}
