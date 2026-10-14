package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone checkpoints by code. */
interface ZoneCheckpointStore {
    fun find(code: String): ZoneCheckpoint?
    fun save(item: ZoneCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneCheckpointStore(
    var items: MutableMap<String, ZoneCheckpoint>,
    var order: MutableList<String>,
) : ZoneCheckpointStore {
    override fun find(code: String): ZoneCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneCheckpoint) {
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

    override fun list(): MutableList<ZoneCheckpoint> {
        val out = mutableListOf<ZoneCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneCheckpointStore(): ZoneCheckpointStore {
    return MemoryZoneCheckpointStore(mutableMapOf<String, ZoneCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneCheckpointStore(
    var inner: ZoneCheckpointStore,
    var cache: TtlCache<ZoneCheckpoint>,
    var hits: Int,
    var misses: Int,
) : ZoneCheckpointStore {
    override fun find(code: String): ZoneCheckpoint? {
        val hit = (cache.get(code) as ZoneCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneCheckpoint> = inner.list()
}

fun newCachedZoneCheckpointStore(inner: ZoneCheckpointStore, ttl: Int, clock: Clock): ZoneCheckpointStore {
    return CachedZoneCheckpointStore(
        inner = inner,
        cache = newTtlCache<ZoneCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone checkpoints. Empty fields match all. */
data class ZoneCheckpointQuery(
    var label: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneCheckpoint): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<ZoneCheckpoint>): MutableList<ZoneCheckpoint> {
        val matched = filter(items) { candidate: ZoneCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneCheckpoints(store: ZoneCheckpointStore, query: ZoneCheckpointQuery): MutableList<ZoneCheckpoint> {
    return query.apply(store.list())
}
