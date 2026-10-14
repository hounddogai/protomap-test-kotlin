package com.acme.transport.emissions.evcharge.chargeclaim

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict ev charge checkpoints by code. */
interface StrictEvChargeCheckpointStore {
    fun find(code: String): StrictEvChargeCheckpoint?
    fun save(item: StrictEvChargeCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictEvChargeCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictEvChargeCheckpointStore(
    var items: MutableMap<String, StrictEvChargeCheckpoint>,
    var order: MutableList<String>,
) : StrictEvChargeCheckpointStore {
    override fun find(code: String): StrictEvChargeCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictEvChargeCheckpoint) {
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

    override fun list(): MutableList<StrictEvChargeCheckpoint> {
        val out = mutableListOf<StrictEvChargeCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictEvChargeCheckpointStore(): StrictEvChargeCheckpointStore {
    return MemoryStrictEvChargeCheckpointStore(
        mutableMapOf<String, StrictEvChargeCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictEvChargeCheckpointStore(
    var inner: StrictEvChargeCheckpointStore,
    var cache: TtlCache<StrictEvChargeCheckpoint>,
    var hits: Int,
    var misses: Int,
) : StrictEvChargeCheckpointStore {
    override fun find(code: String): StrictEvChargeCheckpoint? {
        val hit = (cache.get(code) as StrictEvChargeCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictEvChargeCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictEvChargeCheckpoint> = inner.list()
}

fun newCachedStrictEvChargeCheckpointStore(inner: StrictEvChargeCheckpointStore, ttl: Int, clock: Clock): StrictEvChargeCheckpointStore {
    return CachedStrictEvChargeCheckpointStore(
        inner = inner,
        cache = newTtlCache<StrictEvChargeCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict ev charge checkpoints. Empty fields match all. */
data class StrictEvChargeCheckpointQuery(
    var channel: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictEvChargeCheckpoint): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictEvChargeCheckpoint>): MutableList<StrictEvChargeCheckpoint> {
        val matched = filter(items) { candidate: StrictEvChargeCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictEvChargeCheckpoints(store: StrictEvChargeCheckpointStore, query: StrictEvChargeCheckpointQuery): MutableList<StrictEvChargeCheckpoint> {
    return query.apply(store.list())
}
