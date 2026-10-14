package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss checkpoints by code. */
interface NearMissCheckpointStore {
    fun find(code: String): NearMissCheckpoint?
    fun save(item: NearMissCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissCheckpointStore(
    var items: MutableMap<String, NearMissCheckpoint>,
    var order: MutableList<String>,
) : NearMissCheckpointStore {
    override fun find(code: String): NearMissCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissCheckpoint) {
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

    override fun list(): MutableList<NearMissCheckpoint> {
        val out = mutableListOf<NearMissCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissCheckpointStore(): NearMissCheckpointStore {
    return MemoryNearMissCheckpointStore(mutableMapOf<String, NearMissCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissCheckpointStore(
    var inner: NearMissCheckpointStore,
    var cache: TtlCache<NearMissCheckpoint>,
    var hits: Int,
    var misses: Int,
) : NearMissCheckpointStore {
    override fun find(code: String): NearMissCheckpoint? {
        val hit = (cache.get(code) as NearMissCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissCheckpoint> = inner.list()
}

fun newCachedNearMissCheckpointStore(inner: NearMissCheckpointStore, ttl: Int, clock: Clock): NearMissCheckpointStore {
    return CachedNearMissCheckpointStore(
        inner = inner,
        cache = newTtlCache<NearMissCheckpoint>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss checkpoints. Empty fields match all. */
data class NearMissCheckpointQuery(
    var label: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissCheckpoint): Boolean {
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

    fun apply(items: MutableList<NearMissCheckpoint>): MutableList<NearMissCheckpoint> {
        val matched = filter(items) { candidate: NearMissCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissCheckpoints(store: NearMissCheckpointStore, query: NearMissCheckpointQuery): MutableList<NearMissCheckpoint> {
    return query.apply(store.list())
}
