package com.acme.transport.fleetops.leasereturn.template

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return checkpoints by code. */
interface LeaseReturnCheckpointStore {
    fun find(code: String): LeaseReturnCheckpoint?
    fun save(item: LeaseReturnCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnCheckpointStore(
    var items: MutableMap<String, LeaseReturnCheckpoint>,
    var order: MutableList<String>,
) : LeaseReturnCheckpointStore {
    override fun find(code: String): LeaseReturnCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnCheckpoint) {
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

    override fun list(): MutableList<LeaseReturnCheckpoint> {
        val out = mutableListOf<LeaseReturnCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnCheckpointStore(): LeaseReturnCheckpointStore {
    return MemoryLeaseReturnCheckpointStore(
        mutableMapOf<String, LeaseReturnCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnCheckpointStore(
    var inner: LeaseReturnCheckpointStore,
    var cache: TtlCache<LeaseReturnCheckpoint>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnCheckpointStore {
    override fun find(code: String): LeaseReturnCheckpoint? {
        val hit = (cache.get(code) as LeaseReturnCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnCheckpoint> = inner.list()
}

fun newCachedLeaseReturnCheckpointStore(inner: LeaseReturnCheckpointStore, ttl: Int, clock: Clock): LeaseReturnCheckpointStore {
    return CachedLeaseReturnCheckpointStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnCheckpoint>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return checkpoints. Empty fields match all. */
data class LeaseReturnCheckpointQuery(
    var country: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnCheckpoint): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnCheckpoint>): MutableList<LeaseReturnCheckpoint> {
        val matched = filter(items) { candidate: LeaseReturnCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnCheckpoints(store: LeaseReturnCheckpointStore, query: LeaseReturnCheckpointQuery): MutableList<LeaseReturnCheckpoint> {
    return query.apply(store.list())
}
