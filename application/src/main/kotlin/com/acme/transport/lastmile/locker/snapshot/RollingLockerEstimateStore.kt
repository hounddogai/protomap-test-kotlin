package com.acme.transport.lastmile.locker.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps rolling locker estimates by code. */
interface RollingLockerEstimateStore {
    fun find(code: String): RollingLockerEstimate?
    fun save(item: RollingLockerEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingLockerEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingLockerEstimateStore(
    var items: MutableMap<String, RollingLockerEstimate>,
    var order: MutableList<String>,
) : RollingLockerEstimateStore {
    override fun find(code: String): RollingLockerEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingLockerEstimate) {
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

    override fun list(): MutableList<RollingLockerEstimate> {
        val out = mutableListOf<RollingLockerEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingLockerEstimateStore(): RollingLockerEstimateStore {
    return MemoryRollingLockerEstimateStore(
        mutableMapOf<String, RollingLockerEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingLockerEstimateStore(
    var inner: RollingLockerEstimateStore,
    var cache: TtlCache<RollingLockerEstimate>,
    var hits: Int,
    var misses: Int,
) : RollingLockerEstimateStore {
    override fun find(code: String): RollingLockerEstimate? {
        val hit = (cache.get(code) as RollingLockerEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingLockerEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingLockerEstimate> = inner.list()
}

fun newCachedRollingLockerEstimateStore(inner: RollingLockerEstimateStore, ttl: Int, clock: Clock): RollingLockerEstimateStore {
    return CachedRollingLockerEstimateStore(
        inner = inner,
        cache = newTtlCache<RollingLockerEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling locker estimates. Empty fields match all. */
data class RollingLockerEstimateQuery(
    var lane: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingLockerEstimate): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<RollingLockerEstimate>): MutableList<RollingLockerEstimate> {
        val matched = filter(items) { candidate: RollingLockerEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingLockerEstimates(store: RollingLockerEstimateStore, query: RollingLockerEstimateQuery): MutableList<RollingLockerEstimate> {
    return query.apply(store.list())
}
