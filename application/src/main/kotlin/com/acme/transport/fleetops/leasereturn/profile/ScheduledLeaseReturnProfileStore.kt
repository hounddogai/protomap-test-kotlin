package com.acme.transport.fleetops.leasereturn.profile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled lease return profiles by code. */
interface ScheduledLeaseReturnProfileStore {
    fun find(code: String): ScheduledLeaseReturnProfile?
    fun save(item: ScheduledLeaseReturnProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledLeaseReturnProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledLeaseReturnProfileStore(
    var items: MutableMap<String, ScheduledLeaseReturnProfile>,
    var order: MutableList<String>,
) : ScheduledLeaseReturnProfileStore {
    override fun find(code: String): ScheduledLeaseReturnProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledLeaseReturnProfile) {
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

    override fun list(): MutableList<ScheduledLeaseReturnProfile> {
        val out = mutableListOf<ScheduledLeaseReturnProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledLeaseReturnProfileStore(): ScheduledLeaseReturnProfileStore {
    return MemoryScheduledLeaseReturnProfileStore(
        mutableMapOf<String, ScheduledLeaseReturnProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledLeaseReturnProfileStore(
    var inner: ScheduledLeaseReturnProfileStore,
    var cache: TtlCache<ScheduledLeaseReturnProfile>,
    var hits: Int,
    var misses: Int,
) : ScheduledLeaseReturnProfileStore {
    override fun find(code: String): ScheduledLeaseReturnProfile? {
        val hit = (cache.get(code) as ScheduledLeaseReturnProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledLeaseReturnProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledLeaseReturnProfile> = inner.list()
}

fun newCachedScheduledLeaseReturnProfileStore(inner: ScheduledLeaseReturnProfileStore, ttl: Int, clock: Clock): ScheduledLeaseReturnProfileStore {
    return CachedScheduledLeaseReturnProfileStore(
        inner = inner,
        cache = newTtlCache<ScheduledLeaseReturnProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled lease return profiles. Empty fields match all. */
data class ScheduledLeaseReturnProfileQuery(
    var segment: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledLeaseReturnProfile): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledLeaseReturnProfile>): MutableList<ScheduledLeaseReturnProfile> {
        val matched = filter(items) { candidate: ScheduledLeaseReturnProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledLeaseReturnProfiles(store: ScheduledLeaseReturnProfileStore, query: ScheduledLeaseReturnProfileQuery): MutableList<ScheduledLeaseReturnProfile> {
    return query.apply(store.list())
}
