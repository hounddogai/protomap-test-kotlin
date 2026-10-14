package com.acme.transport.emissions.emissionfactor.cycle

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor checkpoints by code. */
interface EmissionFactorCheckpointStore {
    fun find(code: String): EmissionFactorCheckpoint?
    fun save(item: EmissionFactorCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorCheckpointStore(
    var items: MutableMap<String, EmissionFactorCheckpoint>,
    var order: MutableList<String>,
) : EmissionFactorCheckpointStore {
    override fun find(code: String): EmissionFactorCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorCheckpoint) {
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

    override fun list(): MutableList<EmissionFactorCheckpoint> {
        val out = mutableListOf<EmissionFactorCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorCheckpointStore(): EmissionFactorCheckpointStore {
    return MemoryEmissionFactorCheckpointStore(
        mutableMapOf<String, EmissionFactorCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorCheckpointStore(
    var inner: EmissionFactorCheckpointStore,
    var cache: TtlCache<EmissionFactorCheckpoint>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorCheckpointStore {
    override fun find(code: String): EmissionFactorCheckpoint? {
        val hit = (cache.get(code) as EmissionFactorCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorCheckpoint> = inner.list()
}

fun newCachedEmissionFactorCheckpointStore(inner: EmissionFactorCheckpointStore, ttl: Int, clock: Clock): EmissionFactorCheckpointStore {
    return CachedEmissionFactorCheckpointStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor checkpoints. Empty fields match all. */
data class EmissionFactorCheckpointQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorCheckpoint): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorCheckpoint>): MutableList<EmissionFactorCheckpoint> {
        val matched = filter(items) { candidate: EmissionFactorCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorCheckpoints(store: EmissionFactorCheckpointStore, query: EmissionFactorCheckpointQuery): MutableList<EmissionFactorCheckpoint> {
    return query.apply(store.list())
}
