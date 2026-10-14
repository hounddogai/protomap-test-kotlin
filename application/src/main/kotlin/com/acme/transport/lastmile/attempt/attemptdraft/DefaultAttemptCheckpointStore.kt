package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default attempt checkpoints by code. */
interface DefaultAttemptCheckpointStore {
    fun find(code: String): DefaultAttemptCheckpoint?
    fun save(item: DefaultAttemptCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultAttemptCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultAttemptCheckpointStore(
    var items: MutableMap<String, DefaultAttemptCheckpoint>,
    var order: MutableList<String>,
) : DefaultAttemptCheckpointStore {
    override fun find(code: String): DefaultAttemptCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultAttemptCheckpoint) {
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

    override fun list(): MutableList<DefaultAttemptCheckpoint> {
        val out = mutableListOf<DefaultAttemptCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultAttemptCheckpointStore(): DefaultAttemptCheckpointStore {
    return MemoryDefaultAttemptCheckpointStore(
        mutableMapOf<String, DefaultAttemptCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultAttemptCheckpointStore(
    var inner: DefaultAttemptCheckpointStore,
    var cache: TtlCache<DefaultAttemptCheckpoint>,
    var hits: Int,
    var misses: Int,
) : DefaultAttemptCheckpointStore {
    override fun find(code: String): DefaultAttemptCheckpoint? {
        val hit = (cache.get(code) as DefaultAttemptCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultAttemptCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultAttemptCheckpoint> = inner.list()
}

fun newCachedDefaultAttemptCheckpointStore(inner: DefaultAttemptCheckpointStore, ttl: Int, clock: Clock): DefaultAttemptCheckpointStore {
    return CachedDefaultAttemptCheckpointStore(
        inner = inner,
        cache = newTtlCache<DefaultAttemptCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default attempt checkpoints. Empty fields match all. */
data class DefaultAttemptCheckpointQuery(
    var category: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultAttemptCheckpoint): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<DefaultAttemptCheckpoint>): MutableList<DefaultAttemptCheckpoint> {
        val matched = filter(items) { candidate: DefaultAttemptCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultAttemptCheckpoints(store: DefaultAttemptCheckpointStore, query: DefaultAttemptCheckpointQuery): MutableList<DefaultAttemptCheckpoint> {
    return query.apply(store.list())
}
