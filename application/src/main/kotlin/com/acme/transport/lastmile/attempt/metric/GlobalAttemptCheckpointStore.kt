package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global attempt checkpoints by code. */
interface GlobalAttemptCheckpointStore {
    fun find(code: String): GlobalAttemptCheckpoint?
    fun save(item: GlobalAttemptCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalAttemptCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalAttemptCheckpointStore(
    var items: MutableMap<String, GlobalAttemptCheckpoint>,
    var order: MutableList<String>,
) : GlobalAttemptCheckpointStore {
    override fun find(code: String): GlobalAttemptCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalAttemptCheckpoint) {
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

    override fun list(): MutableList<GlobalAttemptCheckpoint> {
        val out = mutableListOf<GlobalAttemptCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalAttemptCheckpointStore(): GlobalAttemptCheckpointStore {
    return MemoryGlobalAttemptCheckpointStore(
        mutableMapOf<String, GlobalAttemptCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalAttemptCheckpointStore(
    var inner: GlobalAttemptCheckpointStore,
    var cache: TtlCache<GlobalAttemptCheckpoint>,
    var hits: Int,
    var misses: Int,
) : GlobalAttemptCheckpointStore {
    override fun find(code: String): GlobalAttemptCheckpoint? {
        val hit = (cache.get(code) as GlobalAttemptCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalAttemptCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalAttemptCheckpoint> = inner.list()
}

fun newCachedGlobalAttemptCheckpointStore(inner: GlobalAttemptCheckpointStore, ttl: Int, clock: Clock): GlobalAttemptCheckpointStore {
    return CachedGlobalAttemptCheckpointStore(
        inner = inner,
        cache = newTtlCache<GlobalAttemptCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global attempt checkpoints. Empty fields match all. */
data class GlobalAttemptCheckpointQuery(
    var category: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalAttemptCheckpoint): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalAttemptCheckpoint>): MutableList<GlobalAttemptCheckpoint> {
        val matched = filter(items) { candidate: GlobalAttemptCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalAttemptCheckpoints(store: GlobalAttemptCheckpointStore, query: GlobalAttemptCheckpointQuery): MutableList<GlobalAttemptCheckpoint> {
    return query.apply(store.list())
}
