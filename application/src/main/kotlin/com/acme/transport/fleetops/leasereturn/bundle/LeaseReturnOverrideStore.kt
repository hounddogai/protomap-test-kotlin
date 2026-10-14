package com.acme.transport.fleetops.leasereturn.bundle

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return overrides by code. */
interface LeaseReturnOverrideStore {
    fun find(code: String): LeaseReturnOverride?
    fun save(item: LeaseReturnOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnOverrideStore(
    var items: MutableMap<String, LeaseReturnOverride>,
    var order: MutableList<String>,
) : LeaseReturnOverrideStore {
    override fun find(code: String): LeaseReturnOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnOverride) {
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

    override fun list(): MutableList<LeaseReturnOverride> {
        val out = mutableListOf<LeaseReturnOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnOverrideStore(): LeaseReturnOverrideStore {
    return MemoryLeaseReturnOverrideStore(mutableMapOf<String, LeaseReturnOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnOverrideStore(
    var inner: LeaseReturnOverrideStore,
    var cache: TtlCache<LeaseReturnOverride>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnOverrideStore {
    override fun find(code: String): LeaseReturnOverride? {
        val hit = (cache.get(code) as LeaseReturnOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnOverride> = inner.list()
}

fun newCachedLeaseReturnOverrideStore(inner: LeaseReturnOverrideStore, ttl: Int, clock: Clock): LeaseReturnOverrideStore {
    return CachedLeaseReturnOverrideStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnOverride>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return overrides. Empty fields match all. */
data class LeaseReturnOverrideQuery(
    var segment: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnOverride): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnOverride>): MutableList<LeaseReturnOverride> {
        val matched = filter(items) { candidate: LeaseReturnOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnOverrides(store: LeaseReturnOverrideStore, query: LeaseReturnOverrideQuery): MutableList<LeaseReturnOverride> {
    return query.apply(store.list())
}
