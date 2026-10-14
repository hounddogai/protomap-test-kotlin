package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt checkpoints by code. */
interface AttemptCheckpointStore {
    fun find(code: String): AttemptCheckpoint?
    fun save(item: AttemptCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptCheckpointStore(
    var items: MutableMap<String, AttemptCheckpoint>,
    var order: MutableList<String>,
) : AttemptCheckpointStore {
    override fun find(code: String): AttemptCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptCheckpoint) {
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

    override fun list(): MutableList<AttemptCheckpoint> {
        val out = mutableListOf<AttemptCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptCheckpointStore(): AttemptCheckpointStore {
    return MemoryAttemptCheckpointStore(mutableMapOf<String, AttemptCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptCheckpointStore(
    var inner: AttemptCheckpointStore,
    var cache: TtlCache<AttemptCheckpoint>,
    var hits: Int,
    var misses: Int,
) : AttemptCheckpointStore {
    override fun find(code: String): AttemptCheckpoint? {
        val hit = (cache.get(code) as AttemptCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptCheckpoint> = inner.list()
}

fun newCachedAttemptCheckpointStore(inner: AttemptCheckpointStore, ttl: Int, clock: Clock): AttemptCheckpointStore {
    return CachedAttemptCheckpointStore(
        inner = inner,
        cache = newTtlCache<AttemptCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt checkpoints. Empty fields match all. */
data class AttemptCheckpointQuery(
    var channel: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptCheckpoint): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptCheckpoint>): MutableList<AttemptCheckpoint> {
        val matched = filter(items) { candidate: AttemptCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptCheckpoints(store: AttemptCheckpointStore, query: AttemptCheckpointQuery): MutableList<AttemptCheckpoint> {
    return query.apply(store.list())
}
