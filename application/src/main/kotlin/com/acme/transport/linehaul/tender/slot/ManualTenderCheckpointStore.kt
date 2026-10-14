package com.acme.transport.linehaul.tender.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps manual tender checkpoints by code. */
interface ManualTenderCheckpointStore {
    fun find(code: String): ManualTenderCheckpoint?
    fun save(item: ManualTenderCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualTenderCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualTenderCheckpointStore(
    var items: MutableMap<String, ManualTenderCheckpoint>,
    var order: MutableList<String>,
) : ManualTenderCheckpointStore {
    override fun find(code: String): ManualTenderCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualTenderCheckpoint) {
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

    override fun list(): MutableList<ManualTenderCheckpoint> {
        val out = mutableListOf<ManualTenderCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualTenderCheckpointStore(): ManualTenderCheckpointStore {
    return MemoryManualTenderCheckpointStore(
        mutableMapOf<String, ManualTenderCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualTenderCheckpointStore(
    var inner: ManualTenderCheckpointStore,
    var cache: TtlCache<ManualTenderCheckpoint>,
    var hits: Int,
    var misses: Int,
) : ManualTenderCheckpointStore {
    override fun find(code: String): ManualTenderCheckpoint? {
        val hit = (cache.get(code) as ManualTenderCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualTenderCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualTenderCheckpoint> = inner.list()
}

fun newCachedManualTenderCheckpointStore(inner: ManualTenderCheckpointStore, ttl: Int, clock: Clock): ManualTenderCheckpointStore {
    return CachedManualTenderCheckpointStore(
        inner = inner,
        cache = newTtlCache<ManualTenderCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual tender checkpoints. Empty fields match all. */
data class ManualTenderCheckpointQuery(
    var segment: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualTenderCheckpoint): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<ManualTenderCheckpoint>): MutableList<ManualTenderCheckpoint> {
        val matched = filter(items) { candidate: ManualTenderCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualTenderCheckpoints(store: ManualTenderCheckpointStore, query: ManualTenderCheckpointQuery): MutableList<ManualTenderCheckpoint> {
    return query.apply(store.list())
}
