package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional lease return baselines by code. */
interface RegionalLeaseReturnBaselineStore {
    fun find(code: String): RegionalLeaseReturnBaseline?
    fun save(item: RegionalLeaseReturnBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalLeaseReturnBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalLeaseReturnBaselineStore(
    var items: MutableMap<String, RegionalLeaseReturnBaseline>,
    var order: MutableList<String>,
) : RegionalLeaseReturnBaselineStore {
    override fun find(code: String): RegionalLeaseReturnBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalLeaseReturnBaseline) {
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

    override fun list(): MutableList<RegionalLeaseReturnBaseline> {
        val out = mutableListOf<RegionalLeaseReturnBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalLeaseReturnBaselineStore(): RegionalLeaseReturnBaselineStore {
    return MemoryRegionalLeaseReturnBaselineStore(
        mutableMapOf<String, RegionalLeaseReturnBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalLeaseReturnBaselineStore(
    var inner: RegionalLeaseReturnBaselineStore,
    var cache: TtlCache<RegionalLeaseReturnBaseline>,
    var hits: Int,
    var misses: Int,
) : RegionalLeaseReturnBaselineStore {
    override fun find(code: String): RegionalLeaseReturnBaseline? {
        val hit = (cache.get(code) as RegionalLeaseReturnBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalLeaseReturnBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalLeaseReturnBaseline> = inner.list()
}

fun newCachedRegionalLeaseReturnBaselineStore(inner: RegionalLeaseReturnBaselineStore, ttl: Int, clock: Clock): RegionalLeaseReturnBaselineStore {
    return CachedRegionalLeaseReturnBaselineStore(
        inner = inner,
        cache = newTtlCache<RegionalLeaseReturnBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional lease return baselines. Empty fields match all. */
data class RegionalLeaseReturnBaselineQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalLeaseReturnBaseline): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalLeaseReturnBaseline>): MutableList<RegionalLeaseReturnBaseline> {
        val matched = filter(items) { candidate: RegionalLeaseReturnBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalLeaseReturnBaselines(store: RegionalLeaseReturnBaselineStore, query: RegionalLeaseReturnBaselineQuery): MutableList<RegionalLeaseReturnBaseline> {
    return query.apply(store.list())
}
