package com.acme.transport.safety.coaching.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching checkpoints by code. */
interface CoachingCheckpointStore {
    fun find(code: String): CoachingCheckpoint?
    fun save(item: CoachingCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingCheckpointStore(
    var items: MutableMap<String, CoachingCheckpoint>,
    var order: MutableList<String>,
) : CoachingCheckpointStore {
    override fun find(code: String): CoachingCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingCheckpoint) {
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

    override fun list(): MutableList<CoachingCheckpoint> {
        val out = mutableListOf<CoachingCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingCheckpointStore(): CoachingCheckpointStore {
    return MemoryCoachingCheckpointStore(mutableMapOf<String, CoachingCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingCheckpointStore(
    var inner: CoachingCheckpointStore,
    var cache: TtlCache<CoachingCheckpoint>,
    var hits: Int,
    var misses: Int,
) : CoachingCheckpointStore {
    override fun find(code: String): CoachingCheckpoint? {
        val hit = (cache.get(code) as CoachingCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingCheckpoint> = inner.list()
}

fun newCachedCoachingCheckpointStore(inner: CoachingCheckpointStore, ttl: Int, clock: Clock): CoachingCheckpointStore {
    return CachedCoachingCheckpointStore(
        inner = inner,
        cache = newTtlCache<CoachingCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching checkpoints. Empty fields match all. */
data class CoachingCheckpointQuery(
    var depot: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingCheckpoint): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingCheckpoint>): MutableList<CoachingCheckpoint> {
        val matched = filter(items) { candidate: CoachingCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingCheckpoints(store: CoachingCheckpointStore, query: CoachingCheckpointQuery): MutableList<CoachingCheckpoint> {
    return query.apply(store.list())
}
