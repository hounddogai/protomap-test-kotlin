package com.acme.transport.emissions.evcharge.preference

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge checkpoints by code. */
interface EvChargeCheckpointStore {
    fun find(code: String): EvChargeCheckpoint?
    fun save(item: EvChargeCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeCheckpointStore(
    var items: MutableMap<String, EvChargeCheckpoint>,
    var order: MutableList<String>,
) : EvChargeCheckpointStore {
    override fun find(code: String): EvChargeCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeCheckpoint) {
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

    override fun list(): MutableList<EvChargeCheckpoint> {
        val out = mutableListOf<EvChargeCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeCheckpointStore(): EvChargeCheckpointStore {
    return MemoryEvChargeCheckpointStore(mutableMapOf<String, EvChargeCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeCheckpointStore(
    var inner: EvChargeCheckpointStore,
    var cache: TtlCache<EvChargeCheckpoint>,
    var hits: Int,
    var misses: Int,
) : EvChargeCheckpointStore {
    override fun find(code: String): EvChargeCheckpoint? {
        val hit = (cache.get(code) as EvChargeCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeCheckpoint> = inner.list()
}

fun newCachedEvChargeCheckpointStore(inner: EvChargeCheckpointStore, ttl: Int, clock: Clock): EvChargeCheckpointStore {
    return CachedEvChargeCheckpointStore(
        inner = inner,
        cache = newTtlCache<EvChargeCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge checkpoints. Empty fields match all. */
data class EvChargeCheckpointQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeCheckpoint): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeCheckpoint>): MutableList<EvChargeCheckpoint> {
        val matched = filter(items) { candidate: EvChargeCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeCheckpoints(store: EvChargeCheckpointStore, query: EvChargeCheckpointQuery): MutableList<EvChargeCheckpoint> {
    return query.apply(store.list())
}
