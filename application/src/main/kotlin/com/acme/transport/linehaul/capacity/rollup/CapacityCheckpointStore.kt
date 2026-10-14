package com.acme.transport.linehaul.capacity.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity checkpoints by code. */
interface CapacityCheckpointStore {
    fun find(code: String): CapacityCheckpoint?
    fun save(item: CapacityCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityCheckpointStore(
    var items: MutableMap<String, CapacityCheckpoint>,
    var order: MutableList<String>,
) : CapacityCheckpointStore {
    override fun find(code: String): CapacityCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityCheckpoint) {
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

    override fun list(): MutableList<CapacityCheckpoint> {
        val out = mutableListOf<CapacityCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityCheckpointStore(): CapacityCheckpointStore {
    return MemoryCapacityCheckpointStore(mutableMapOf<String, CapacityCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityCheckpointStore(
    var inner: CapacityCheckpointStore,
    var cache: TtlCache<CapacityCheckpoint>,
    var hits: Int,
    var misses: Int,
) : CapacityCheckpointStore {
    override fun find(code: String): CapacityCheckpoint? {
        val hit = (cache.get(code) as CapacityCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityCheckpoint> = inner.list()
}

fun newCachedCapacityCheckpointStore(inner: CapacityCheckpointStore, ttl: Int, clock: Clock): CapacityCheckpointStore {
    return CachedCapacityCheckpointStore(
        inner = inner,
        cache = newTtlCache<CapacityCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity checkpoints. Empty fields match all. */
data class CapacityCheckpointQuery(
    var country: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityCheckpoint): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityCheckpoint>): MutableList<CapacityCheckpoint> {
        val matched = filter(items) { candidate: CapacityCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityCheckpoints(store: CapacityCheckpointStore, query: CapacityCheckpointQuery): MutableList<CapacityCheckpoint> {
    return query.apply(store.list())
}
