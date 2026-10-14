package com.acme.transport.linehaul.capacity.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps regional capacity checkpoints by code. */
interface RegionalCapacityCheckpointStore {
    fun find(code: String): RegionalCapacityCheckpoint?
    fun save(item: RegionalCapacityCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCapacityCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCapacityCheckpointStore(
    var items: MutableMap<String, RegionalCapacityCheckpoint>,
    var order: MutableList<String>,
) : RegionalCapacityCheckpointStore {
    override fun find(code: String): RegionalCapacityCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCapacityCheckpoint) {
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

    override fun list(): MutableList<RegionalCapacityCheckpoint> {
        val out = mutableListOf<RegionalCapacityCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCapacityCheckpointStore(): RegionalCapacityCheckpointStore {
    return MemoryRegionalCapacityCheckpointStore(
        mutableMapOf<String, RegionalCapacityCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCapacityCheckpointStore(
    var inner: RegionalCapacityCheckpointStore,
    var cache: TtlCache<RegionalCapacityCheckpoint>,
    var hits: Int,
    var misses: Int,
) : RegionalCapacityCheckpointStore {
    override fun find(code: String): RegionalCapacityCheckpoint? {
        val hit = (cache.get(code) as RegionalCapacityCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCapacityCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCapacityCheckpoint> = inner.list()
}

fun newCachedRegionalCapacityCheckpointStore(inner: RegionalCapacityCheckpointStore, ttl: Int, clock: Clock): RegionalCapacityCheckpointStore {
    return CachedRegionalCapacityCheckpointStore(
        inner = inner,
        cache = newTtlCache<RegionalCapacityCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional capacity checkpoints. Empty fields match all. */
data class RegionalCapacityCheckpointQuery(
    var locale: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCapacityCheckpoint): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalCapacityCheckpoint>): MutableList<RegionalCapacityCheckpoint> {
        val matched = filter(items) { candidate: RegionalCapacityCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCapacityCheckpoints(store: RegionalCapacityCheckpointStore, query: RegionalCapacityCheckpointQuery): MutableList<RegionalCapacityCheckpoint> {
    return query.apply(store.list())
}
