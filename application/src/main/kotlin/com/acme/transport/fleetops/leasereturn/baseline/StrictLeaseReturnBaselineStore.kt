package com.acme.transport.fleetops.leasereturn.baseline

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict lease return baselines by code. */
interface StrictLeaseReturnBaselineStore {
    fun find(code: String): StrictLeaseReturnBaseline?
    fun save(item: StrictLeaseReturnBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictLeaseReturnBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictLeaseReturnBaselineStore(
    var items: MutableMap<String, StrictLeaseReturnBaseline>,
    var order: MutableList<String>,
) : StrictLeaseReturnBaselineStore {
    override fun find(code: String): StrictLeaseReturnBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictLeaseReturnBaseline) {
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

    override fun list(): MutableList<StrictLeaseReturnBaseline> {
        val out = mutableListOf<StrictLeaseReturnBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictLeaseReturnBaselineStore(): StrictLeaseReturnBaselineStore {
    return MemoryStrictLeaseReturnBaselineStore(
        mutableMapOf<String, StrictLeaseReturnBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictLeaseReturnBaselineStore(
    var inner: StrictLeaseReturnBaselineStore,
    var cache: TtlCache<StrictLeaseReturnBaseline>,
    var hits: Int,
    var misses: Int,
) : StrictLeaseReturnBaselineStore {
    override fun find(code: String): StrictLeaseReturnBaseline? {
        val hit = (cache.get(code) as StrictLeaseReturnBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictLeaseReturnBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictLeaseReturnBaseline> = inner.list()
}

fun newCachedStrictLeaseReturnBaselineStore(inner: StrictLeaseReturnBaselineStore, ttl: Int, clock: Clock): StrictLeaseReturnBaselineStore {
    return CachedStrictLeaseReturnBaselineStore(
        inner = inner,
        cache = newTtlCache<StrictLeaseReturnBaseline>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict lease return baselines. Empty fields match all. */
data class StrictLeaseReturnBaselineQuery(
    var category: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictLeaseReturnBaseline): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictLeaseReturnBaseline>): MutableList<StrictLeaseReturnBaseline> {
        val matched = filter(items) { candidate: StrictLeaseReturnBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictLeaseReturnBaselines(store: StrictLeaseReturnBaselineStore, query: StrictLeaseReturnBaselineQuery): MutableList<StrictLeaseReturnBaseline> {
    return query.apply(store.list())
}
