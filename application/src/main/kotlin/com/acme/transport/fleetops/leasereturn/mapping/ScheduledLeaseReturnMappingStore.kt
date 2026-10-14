package com.acme.transport.fleetops.leasereturn.mapping

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled lease return mappings by code. */
interface ScheduledLeaseReturnMappingStore {
    fun find(code: String): ScheduledLeaseReturnMapping?
    fun save(item: ScheduledLeaseReturnMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledLeaseReturnMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledLeaseReturnMappingStore(
    var items: MutableMap<String, ScheduledLeaseReturnMapping>,
    var order: MutableList<String>,
) : ScheduledLeaseReturnMappingStore {
    override fun find(code: String): ScheduledLeaseReturnMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledLeaseReturnMapping) {
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

    override fun list(): MutableList<ScheduledLeaseReturnMapping> {
        val out = mutableListOf<ScheduledLeaseReturnMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledLeaseReturnMappingStore(): ScheduledLeaseReturnMappingStore {
    return MemoryScheduledLeaseReturnMappingStore(
        mutableMapOf<String, ScheduledLeaseReturnMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledLeaseReturnMappingStore(
    var inner: ScheduledLeaseReturnMappingStore,
    var cache: TtlCache<ScheduledLeaseReturnMapping>,
    var hits: Int,
    var misses: Int,
) : ScheduledLeaseReturnMappingStore {
    override fun find(code: String): ScheduledLeaseReturnMapping? {
        val hit = (cache.get(code) as ScheduledLeaseReturnMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledLeaseReturnMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledLeaseReturnMapping> = inner.list()
}

fun newCachedScheduledLeaseReturnMappingStore(inner: ScheduledLeaseReturnMappingStore, ttl: Int, clock: Clock): ScheduledLeaseReturnMappingStore {
    return CachedScheduledLeaseReturnMappingStore(
        inner = inner,
        cache = newTtlCache<ScheduledLeaseReturnMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled lease return mappings. Empty fields match all. */
data class ScheduledLeaseReturnMappingQuery(
    var depot: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledLeaseReturnMapping): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledLeaseReturnMapping>): MutableList<ScheduledLeaseReturnMapping> {
        val matched = filter(items) { candidate: ScheduledLeaseReturnMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledLeaseReturnMappings(store: ScheduledLeaseReturnMappingStore, query: ScheduledLeaseReturnMappingQuery): MutableList<ScheduledLeaseReturnMapping> {
    return query.apply(store.list())
}
