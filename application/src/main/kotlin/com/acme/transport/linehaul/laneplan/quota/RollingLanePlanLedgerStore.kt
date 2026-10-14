package com.acme.transport.linehaul.laneplan.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps rolling lane plan ledgers by code. */
interface RollingLanePlanLedgerStore {
    fun find(code: String): RollingLanePlanLedger?
    fun save(item: RollingLanePlanLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingLanePlanLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingLanePlanLedgerStore(
    var items: MutableMap<String, RollingLanePlanLedger>,
    var order: MutableList<String>,
) : RollingLanePlanLedgerStore {
    override fun find(code: String): RollingLanePlanLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingLanePlanLedger) {
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

    override fun list(): MutableList<RollingLanePlanLedger> {
        val out = mutableListOf<RollingLanePlanLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingLanePlanLedgerStore(): RollingLanePlanLedgerStore {
    return MemoryRollingLanePlanLedgerStore(
        mutableMapOf<String, RollingLanePlanLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingLanePlanLedgerStore(
    var inner: RollingLanePlanLedgerStore,
    var cache: TtlCache<RollingLanePlanLedger>,
    var hits: Int,
    var misses: Int,
) : RollingLanePlanLedgerStore {
    override fun find(code: String): RollingLanePlanLedger? {
        val hit = (cache.get(code) as RollingLanePlanLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingLanePlanLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingLanePlanLedger> = inner.list()
}

fun newCachedRollingLanePlanLedgerStore(inner: RollingLanePlanLedgerStore, ttl: Int, clock: Clock): RollingLanePlanLedgerStore {
    return CachedRollingLanePlanLedgerStore(
        inner = inner,
        cache = newTtlCache<RollingLanePlanLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling lane plan ledgers. Empty fields match all. */
data class RollingLanePlanLedgerQuery(
    var title: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingLanePlanLedger): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingLanePlanLedger>): MutableList<RollingLanePlanLedger> {
        val matched = filter(items) { candidate: RollingLanePlanLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingLanePlanLedgers(store: RollingLanePlanLedgerStore, query: RollingLanePlanLedgerQuery): MutableList<RollingLanePlanLedger> {
    return query.apply(store.list())
}
