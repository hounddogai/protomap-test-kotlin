package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return profiles by code. */
interface LeaseReturnProfileStore {
    fun find(code: String): LeaseReturnProfile?
    fun save(item: LeaseReturnProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnProfileStore(
    var items: MutableMap<String, LeaseReturnProfile>,
    var order: MutableList<String>,
) : LeaseReturnProfileStore {
    override fun find(code: String): LeaseReturnProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnProfile) {
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

    override fun list(): MutableList<LeaseReturnProfile> {
        val out = mutableListOf<LeaseReturnProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnProfileStore(): LeaseReturnProfileStore {
    return MemoryLeaseReturnProfileStore(mutableMapOf<String, LeaseReturnProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnProfileStore(
    var inner: LeaseReturnProfileStore,
    var cache: TtlCache<LeaseReturnProfile>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnProfileStore {
    override fun find(code: String): LeaseReturnProfile? {
        val hit = (cache.get(code) as LeaseReturnProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnProfile> = inner.list()
}

fun newCachedLeaseReturnProfileStore(inner: LeaseReturnProfileStore, ttl: Int, clock: Clock): LeaseReturnProfileStore {
    return CachedLeaseReturnProfileStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return profiles. Empty fields match all. */
data class LeaseReturnProfileQuery(
    var source: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnProfile): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnProfile>): MutableList<LeaseReturnProfile> {
        val matched = filter(items) { candidate: LeaseReturnProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnProfiles(store: LeaseReturnProfileStore, query: LeaseReturnProfileQuery): MutableList<LeaseReturnProfile> {
    return query.apply(store.list())
}
