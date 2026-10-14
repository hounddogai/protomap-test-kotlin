package com.acme.transport.fleetops.serviceplan.planledger

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default service plan ledgers by code. */
interface DefaultServicePlanLedgerStore {
    fun find(code: String): DefaultServicePlanLedger?
    fun save(item: DefaultServicePlanLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultServicePlanLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultServicePlanLedgerStore(
    var items: MutableMap<String, DefaultServicePlanLedger>,
    var order: MutableList<String>,
) : DefaultServicePlanLedgerStore {
    override fun find(code: String): DefaultServicePlanLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultServicePlanLedger) {
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

    override fun list(): MutableList<DefaultServicePlanLedger> {
        val out = mutableListOf<DefaultServicePlanLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultServicePlanLedgerStore(): DefaultServicePlanLedgerStore {
    return MemoryDefaultServicePlanLedgerStore(
        mutableMapOf<String, DefaultServicePlanLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultServicePlanLedgerStore(
    var inner: DefaultServicePlanLedgerStore,
    var cache: TtlCache<DefaultServicePlanLedger>,
    var hits: Int,
    var misses: Int,
) : DefaultServicePlanLedgerStore {
    override fun find(code: String): DefaultServicePlanLedger? {
        val hit = (cache.get(code) as DefaultServicePlanLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultServicePlanLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultServicePlanLedger> = inner.list()
}

fun newCachedDefaultServicePlanLedgerStore(inner: DefaultServicePlanLedgerStore, ttl: Int, clock: Clock): DefaultServicePlanLedgerStore {
    return CachedDefaultServicePlanLedgerStore(
        inner = inner,
        cache = newTtlCache<DefaultServicePlanLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default service plan ledgers. Empty fields match all. */
data class DefaultServicePlanLedgerQuery(
    var region: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultServicePlanLedger): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultServicePlanLedger>): MutableList<DefaultServicePlanLedger> {
        val matched = filter(items) { candidate: DefaultServicePlanLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultServicePlanLedgers(store: DefaultServicePlanLedgerStore, query: DefaultServicePlanLedgerQuery): MutableList<DefaultServicePlanLedger> {
    return query.apply(store.list())
}
