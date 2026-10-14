package com.acme.transport.emissions.idling.target

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling checkpoints by code. */
interface IdlingCheckpointStore {
    fun find(code: String): IdlingCheckpoint?
    fun save(item: IdlingCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingCheckpointStore(
    var items: MutableMap<String, IdlingCheckpoint>,
    var order: MutableList<String>,
) : IdlingCheckpointStore {
    override fun find(code: String): IdlingCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingCheckpoint) {
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

    override fun list(): MutableList<IdlingCheckpoint> {
        val out = mutableListOf<IdlingCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingCheckpointStore(): IdlingCheckpointStore {
    return MemoryIdlingCheckpointStore(mutableMapOf<String, IdlingCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingCheckpointStore(
    var inner: IdlingCheckpointStore,
    var cache: TtlCache<IdlingCheckpoint>,
    var hits: Int,
    var misses: Int,
) : IdlingCheckpointStore {
    override fun find(code: String): IdlingCheckpoint? {
        val hit = (cache.get(code) as IdlingCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingCheckpoint> = inner.list()
}

fun newCachedIdlingCheckpointStore(inner: IdlingCheckpointStore, ttl: Int, clock: Clock): IdlingCheckpointStore {
    return CachedIdlingCheckpointStore(
        inner = inner,
        cache = newTtlCache<IdlingCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling checkpoints. Empty fields match all. */
data class IdlingCheckpointQuery(
    var label: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingCheckpoint): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingCheckpoint>): MutableList<IdlingCheckpoint> {
        val matched = filter(items) { candidate: IdlingCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingCheckpoints(store: IdlingCheckpointStore, query: IdlingCheckpointQuery): MutableList<IdlingCheckpoint> {
    return query.apply(store.list())
}
