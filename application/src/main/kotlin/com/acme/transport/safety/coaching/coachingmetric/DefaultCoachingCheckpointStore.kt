package com.acme.transport.safety.coaching.coachingmetric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps default coaching checkpoints by code. */
interface DefaultCoachingCheckpointStore {
    fun find(code: String): DefaultCoachingCheckpoint?
    fun save(item: DefaultCoachingCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCoachingCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCoachingCheckpointStore(
    var items: MutableMap<String, DefaultCoachingCheckpoint>,
    var order: MutableList<String>,
) : DefaultCoachingCheckpointStore {
    override fun find(code: String): DefaultCoachingCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCoachingCheckpoint) {
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

    override fun list(): MutableList<DefaultCoachingCheckpoint> {
        val out = mutableListOf<DefaultCoachingCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCoachingCheckpointStore(): DefaultCoachingCheckpointStore {
    return MemoryDefaultCoachingCheckpointStore(
        mutableMapOf<String, DefaultCoachingCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCoachingCheckpointStore(
    var inner: DefaultCoachingCheckpointStore,
    var cache: TtlCache<DefaultCoachingCheckpoint>,
    var hits: Int,
    var misses: Int,
) : DefaultCoachingCheckpointStore {
    override fun find(code: String): DefaultCoachingCheckpoint? {
        val hit = (cache.get(code) as DefaultCoachingCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCoachingCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCoachingCheckpoint> = inner.list()
}

fun newCachedDefaultCoachingCheckpointStore(inner: DefaultCoachingCheckpointStore, ttl: Int, clock: Clock): DefaultCoachingCheckpointStore {
    return CachedDefaultCoachingCheckpointStore(
        inner = inner,
        cache = newTtlCache<DefaultCoachingCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default coaching checkpoints. Empty fields match all. */
data class DefaultCoachingCheckpointQuery(
    var label: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCoachingCheckpoint): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultCoachingCheckpoint>): MutableList<DefaultCoachingCheckpoint> {
        val matched = filter(items) { candidate: DefaultCoachingCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCoachingCheckpoints(store: DefaultCoachingCheckpointStore, query: DefaultCoachingCheckpointQuery): MutableList<DefaultCoachingCheckpoint> {
    return query.apply(store.list())
}
