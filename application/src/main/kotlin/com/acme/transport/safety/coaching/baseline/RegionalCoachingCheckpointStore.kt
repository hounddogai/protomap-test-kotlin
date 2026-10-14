package com.acme.transport.safety.coaching.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps regional coaching checkpoints by code. */
interface RegionalCoachingCheckpointStore {
    fun find(code: String): RegionalCoachingCheckpoint?
    fun save(item: RegionalCoachingCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCoachingCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCoachingCheckpointStore(
    var items: MutableMap<String, RegionalCoachingCheckpoint>,
    var order: MutableList<String>,
) : RegionalCoachingCheckpointStore {
    override fun find(code: String): RegionalCoachingCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCoachingCheckpoint) {
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

    override fun list(): MutableList<RegionalCoachingCheckpoint> {
        val out = mutableListOf<RegionalCoachingCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCoachingCheckpointStore(): RegionalCoachingCheckpointStore {
    return MemoryRegionalCoachingCheckpointStore(
        mutableMapOf<String, RegionalCoachingCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCoachingCheckpointStore(
    var inner: RegionalCoachingCheckpointStore,
    var cache: TtlCache<RegionalCoachingCheckpoint>,
    var hits: Int,
    var misses: Int,
) : RegionalCoachingCheckpointStore {
    override fun find(code: String): RegionalCoachingCheckpoint? {
        val hit = (cache.get(code) as RegionalCoachingCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCoachingCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCoachingCheckpoint> = inner.list()
}

fun newCachedRegionalCoachingCheckpointStore(inner: RegionalCoachingCheckpointStore, ttl: Int, clock: Clock): RegionalCoachingCheckpointStore {
    return CachedRegionalCoachingCheckpointStore(
        inner = inner,
        cache = newTtlCache<RegionalCoachingCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional coaching checkpoints. Empty fields match all. */
data class RegionalCoachingCheckpointQuery(
    var label: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCoachingCheckpoint): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalCoachingCheckpoint>): MutableList<RegionalCoachingCheckpoint> {
        val matched = filter(items) { candidate: RegionalCoachingCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCoachingCheckpoints(store: RegionalCoachingCheckpointStore, query: RegionalCoachingCheckpointQuery): MutableList<RegionalCoachingCheckpoint> {
    return query.apply(store.list())
}
