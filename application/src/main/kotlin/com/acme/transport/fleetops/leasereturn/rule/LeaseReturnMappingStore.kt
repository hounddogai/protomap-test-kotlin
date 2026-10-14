package com.acme.transport.fleetops.leasereturn.rule

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return mappings by code. */
interface LeaseReturnMappingStore {
    fun find(code: String): LeaseReturnMapping?
    fun save(item: LeaseReturnMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnMappingStore(
    var items: MutableMap<String, LeaseReturnMapping>,
    var order: MutableList<String>,
) : LeaseReturnMappingStore {
    override fun find(code: String): LeaseReturnMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnMapping) {
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

    override fun list(): MutableList<LeaseReturnMapping> {
        val out = mutableListOf<LeaseReturnMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnMappingStore(): LeaseReturnMappingStore {
    return MemoryLeaseReturnMappingStore(mutableMapOf<String, LeaseReturnMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnMappingStore(
    var inner: LeaseReturnMappingStore,
    var cache: TtlCache<LeaseReturnMapping>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnMappingStore {
    override fun find(code: String): LeaseReturnMapping? {
        val hit = (cache.get(code) as LeaseReturnMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnMapping> = inner.list()
}

fun newCachedLeaseReturnMappingStore(inner: LeaseReturnMappingStore, ttl: Int, clock: Clock): LeaseReturnMappingStore {
    return CachedLeaseReturnMappingStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnMapping>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return mappings. Empty fields match all. */
data class LeaseReturnMappingQuery(
    var country: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnMapping): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnMapping>): MutableList<LeaseReturnMapping> {
        val matched = filter(items) { candidate: LeaseReturnMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnMappings(store: LeaseReturnMappingStore, query: LeaseReturnMappingQuery): MutableList<LeaseReturnMapping> {
    return query.apply(store.list())
}
