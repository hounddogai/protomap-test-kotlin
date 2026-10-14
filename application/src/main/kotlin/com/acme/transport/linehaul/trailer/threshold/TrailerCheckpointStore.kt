package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer checkpoints by code. */
interface TrailerCheckpointStore {
    fun find(code: String): TrailerCheckpoint?
    fun save(item: TrailerCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerCheckpointStore(
    var items: MutableMap<String, TrailerCheckpoint>,
    var order: MutableList<String>,
) : TrailerCheckpointStore {
    override fun find(code: String): TrailerCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerCheckpoint) {
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

    override fun list(): MutableList<TrailerCheckpoint> {
        val out = mutableListOf<TrailerCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerCheckpointStore(): TrailerCheckpointStore {
    return MemoryTrailerCheckpointStore(mutableMapOf<String, TrailerCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerCheckpointStore(
    var inner: TrailerCheckpointStore,
    var cache: TtlCache<TrailerCheckpoint>,
    var hits: Int,
    var misses: Int,
) : TrailerCheckpointStore {
    override fun find(code: String): TrailerCheckpoint? {
        val hit = (cache.get(code) as TrailerCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerCheckpoint> = inner.list()
}

fun newCachedTrailerCheckpointStore(inner: TrailerCheckpointStore, ttl: Int, clock: Clock): TrailerCheckpointStore {
    return CachedTrailerCheckpointStore(
        inner = inner,
        cache = newTtlCache<TrailerCheckpoint>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer checkpoints. Empty fields match all. */
data class TrailerCheckpointQuery(
    var label: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerCheckpoint): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerCheckpoint>): MutableList<TrailerCheckpoint> {
        val matched = filter(items) { candidate: TrailerCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerCheckpoints(store: TrailerCheckpointStore, query: TrailerCheckpointQuery): MutableList<TrailerCheckpoint> {
    return query.apply(store.list())
}
