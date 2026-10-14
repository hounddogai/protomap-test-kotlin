package com.acme.transport.network.zone.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps manual zone checkpoints by code. */
interface ManualZoneCheckpointStore {
    fun find(code: String): ManualZoneCheckpoint?
    fun save(item: ManualZoneCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualZoneCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualZoneCheckpointStore(
    var items: MutableMap<String, ManualZoneCheckpoint>,
    var order: MutableList<String>,
) : ManualZoneCheckpointStore {
    override fun find(code: String): ManualZoneCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualZoneCheckpoint) {
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

    override fun list(): MutableList<ManualZoneCheckpoint> {
        val out = mutableListOf<ManualZoneCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualZoneCheckpointStore(): ManualZoneCheckpointStore {
    return MemoryManualZoneCheckpointStore(
        mutableMapOf<String, ManualZoneCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualZoneCheckpointStore(
    var inner: ManualZoneCheckpointStore,
    var cache: TtlCache<ManualZoneCheckpoint>,
    var hits: Int,
    var misses: Int,
) : ManualZoneCheckpointStore {
    override fun find(code: String): ManualZoneCheckpoint? {
        val hit = (cache.get(code) as ManualZoneCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualZoneCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualZoneCheckpoint> = inner.list()
}

fun newCachedManualZoneCheckpointStore(inner: ManualZoneCheckpointStore, ttl: Int, clock: Clock): ManualZoneCheckpointStore {
    return CachedManualZoneCheckpointStore(
        inner = inner,
        cache = newTtlCache<ManualZoneCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual zone checkpoints. Empty fields match all. */
data class ManualZoneCheckpointQuery(
    var locale: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualZoneCheckpoint): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualZoneCheckpoint>): MutableList<ManualZoneCheckpoint> {
        val matched = filter(items) { candidate: ManualZoneCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualZoneCheckpoints(store: ManualZoneCheckpointStore, query: ManualZoneCheckpointQuery): MutableList<ManualZoneCheckpoint> {
    return query.apply(store.list())
}
