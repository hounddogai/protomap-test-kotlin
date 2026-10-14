package com.acme.transport.network.transittime.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps legacy transit time checkpoints by code. */
interface LegacyTransitTimeCheckpointStore {
    fun find(code: String): LegacyTransitTimeCheckpoint?
    fun save(item: LegacyTransitTimeCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyTransitTimeCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyTransitTimeCheckpointStore(
    var items: MutableMap<String, LegacyTransitTimeCheckpoint>,
    var order: MutableList<String>,
) : LegacyTransitTimeCheckpointStore {
    override fun find(code: String): LegacyTransitTimeCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyTransitTimeCheckpoint) {
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

    override fun list(): MutableList<LegacyTransitTimeCheckpoint> {
        val out = mutableListOf<LegacyTransitTimeCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyTransitTimeCheckpointStore(): LegacyTransitTimeCheckpointStore {
    return MemoryLegacyTransitTimeCheckpointStore(
        mutableMapOf<String, LegacyTransitTimeCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyTransitTimeCheckpointStore(
    var inner: LegacyTransitTimeCheckpointStore,
    var cache: TtlCache<LegacyTransitTimeCheckpoint>,
    var hits: Int,
    var misses: Int,
) : LegacyTransitTimeCheckpointStore {
    override fun find(code: String): LegacyTransitTimeCheckpoint? {
        val hit = (cache.get(code) as LegacyTransitTimeCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyTransitTimeCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyTransitTimeCheckpoint> = inner.list()
}

fun newCachedLegacyTransitTimeCheckpointStore(inner: LegacyTransitTimeCheckpointStore, ttl: Int, clock: Clock): LegacyTransitTimeCheckpointStore {
    return CachedLegacyTransitTimeCheckpointStore(
        inner = inner,
        cache = newTtlCache<LegacyTransitTimeCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy transit time checkpoints. Empty fields match all. */
data class LegacyTransitTimeCheckpointQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyTransitTimeCheckpoint): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyTransitTimeCheckpoint>): MutableList<LegacyTransitTimeCheckpoint> {
        val matched = filter(items) { candidate: LegacyTransitTimeCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyTransitTimeCheckpoints(store: LegacyTransitTimeCheckpointStore, query: LegacyTransitTimeCheckpointQuery): MutableList<LegacyTransitTimeCheckpoint> {
    return query.apply(store.list())
}
