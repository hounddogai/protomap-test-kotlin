package com.acme.transport.fleetops.leasereturn.returnpreference

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global lease return rollups by code. */
interface GlobalLeaseReturnRollupStore {
    fun find(code: String): GlobalLeaseReturnRollup?
    fun save(item: GlobalLeaseReturnRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalLeaseReturnRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalLeaseReturnRollupStore(
    var items: MutableMap<String, GlobalLeaseReturnRollup>,
    var order: MutableList<String>,
) : GlobalLeaseReturnRollupStore {
    override fun find(code: String): GlobalLeaseReturnRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalLeaseReturnRollup) {
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

    override fun list(): MutableList<GlobalLeaseReturnRollup> {
        val out = mutableListOf<GlobalLeaseReturnRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalLeaseReturnRollupStore(): GlobalLeaseReturnRollupStore {
    return MemoryGlobalLeaseReturnRollupStore(
        mutableMapOf<String, GlobalLeaseReturnRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalLeaseReturnRollupStore(
    var inner: GlobalLeaseReturnRollupStore,
    var cache: TtlCache<GlobalLeaseReturnRollup>,
    var hits: Int,
    var misses: Int,
) : GlobalLeaseReturnRollupStore {
    override fun find(code: String): GlobalLeaseReturnRollup? {
        val hit = (cache.get(code) as GlobalLeaseReturnRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalLeaseReturnRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalLeaseReturnRollup> = inner.list()
}

fun newCachedGlobalLeaseReturnRollupStore(inner: GlobalLeaseReturnRollupStore, ttl: Int, clock: Clock): GlobalLeaseReturnRollupStore {
    return CachedGlobalLeaseReturnRollupStore(
        inner = inner,
        cache = newTtlCache<GlobalLeaseReturnRollup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global lease return rollups. Empty fields match all. */
data class GlobalLeaseReturnRollupQuery(
    var unit: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalLeaseReturnRollup): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalLeaseReturnRollup>): MutableList<GlobalLeaseReturnRollup> {
        val matched = filter(items) { candidate: GlobalLeaseReturnRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalLeaseReturnRollups(store: GlobalLeaseReturnRollupStore, query: GlobalLeaseReturnRollupQuery): MutableList<GlobalLeaseReturnRollup> {
    return query.apply(store.list())
}
