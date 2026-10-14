package com.acme.transport.crewing.swap.slot

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling swap ledgers by code. */
interface RollingSwapLedgerStore {
    fun find(code: String): RollingSwapLedger?
    fun save(item: RollingSwapLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingSwapLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingSwapLedgerStore(
    var items: MutableMap<String, RollingSwapLedger>,
    var order: MutableList<String>,
) : RollingSwapLedgerStore {
    override fun find(code: String): RollingSwapLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingSwapLedger) {
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

    override fun list(): MutableList<RollingSwapLedger> {
        val out = mutableListOf<RollingSwapLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingSwapLedgerStore(): RollingSwapLedgerStore {
    return MemoryRollingSwapLedgerStore(mutableMapOf<String, RollingSwapLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingSwapLedgerStore(
    var inner: RollingSwapLedgerStore,
    var cache: TtlCache<RollingSwapLedger>,
    var hits: Int,
    var misses: Int,
) : RollingSwapLedgerStore {
    override fun find(code: String): RollingSwapLedger? {
        val hit = (cache.get(code) as RollingSwapLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingSwapLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingSwapLedger> = inner.list()
}

fun newCachedRollingSwapLedgerStore(inner: RollingSwapLedgerStore, ttl: Int, clock: Clock): RollingSwapLedgerStore {
    return CachedRollingSwapLedgerStore(
        inner = inner,
        cache = newTtlCache<RollingSwapLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling swap ledgers. Empty fields match all. */
data class RollingSwapLedgerQuery(
    var region: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingSwapLedger): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingSwapLedger>): MutableList<RollingSwapLedger> {
        val matched = filter(items) { candidate: RollingSwapLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingSwapLedgers(store: RollingSwapLedgerStore, query: RollingSwapLedgerQuery): MutableList<RollingSwapLedger> {
    return query.apply(store.list())
}
