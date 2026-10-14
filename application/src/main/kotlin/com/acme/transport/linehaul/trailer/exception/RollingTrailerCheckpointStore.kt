package com.acme.transport.linehaul.trailer.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps rolling trailer checkpoints by code. */
interface RollingTrailerCheckpointStore {
    fun find(code: String): RollingTrailerCheckpoint?
    fun save(item: RollingTrailerCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingTrailerCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingTrailerCheckpointStore(
    var items: MutableMap<String, RollingTrailerCheckpoint>,
    var order: MutableList<String>,
) : RollingTrailerCheckpointStore {
    override fun find(code: String): RollingTrailerCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingTrailerCheckpoint) {
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

    override fun list(): MutableList<RollingTrailerCheckpoint> {
        val out = mutableListOf<RollingTrailerCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingTrailerCheckpointStore(): RollingTrailerCheckpointStore {
    return MemoryRollingTrailerCheckpointStore(
        mutableMapOf<String, RollingTrailerCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingTrailerCheckpointStore(
    var inner: RollingTrailerCheckpointStore,
    var cache: TtlCache<RollingTrailerCheckpoint>,
    var hits: Int,
    var misses: Int,
) : RollingTrailerCheckpointStore {
    override fun find(code: String): RollingTrailerCheckpoint? {
        val hit = (cache.get(code) as RollingTrailerCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingTrailerCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingTrailerCheckpoint> = inner.list()
}

fun newCachedRollingTrailerCheckpointStore(inner: RollingTrailerCheckpointStore, ttl: Int, clock: Clock): RollingTrailerCheckpointStore {
    return CachedRollingTrailerCheckpointStore(
        inner = inner,
        cache = newTtlCache<RollingTrailerCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling trailer checkpoints. Empty fields match all. */
data class RollingTrailerCheckpointQuery(
    var channel: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingTrailerCheckpoint): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingTrailerCheckpoint>): MutableList<RollingTrailerCheckpoint> {
        val matched = filter(items) { candidate: RollingTrailerCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingTrailerCheckpoints(store: RollingTrailerCheckpointStore, query: RollingTrailerCheckpointQuery): MutableList<RollingTrailerCheckpoint> {
    return query.apply(store.list())
}
