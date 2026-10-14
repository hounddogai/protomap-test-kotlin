package com.acme.transport.linehaul.tender.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender checkpoints by code. */
interface TenderCheckpointStore {
    fun find(code: String): TenderCheckpoint?
    fun save(item: TenderCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderCheckpointStore(
    var items: MutableMap<String, TenderCheckpoint>,
    var order: MutableList<String>,
) : TenderCheckpointStore {
    override fun find(code: String): TenderCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderCheckpoint) {
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

    override fun list(): MutableList<TenderCheckpoint> {
        val out = mutableListOf<TenderCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderCheckpointStore(): TenderCheckpointStore {
    return MemoryTenderCheckpointStore(mutableMapOf<String, TenderCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderCheckpointStore(
    var inner: TenderCheckpointStore,
    var cache: TtlCache<TenderCheckpoint>,
    var hits: Int,
    var misses: Int,
) : TenderCheckpointStore {
    override fun find(code: String): TenderCheckpoint? {
        val hit = (cache.get(code) as TenderCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderCheckpoint> = inner.list()
}

fun newCachedTenderCheckpointStore(inner: TenderCheckpointStore, ttl: Int, clock: Clock): TenderCheckpointStore {
    return CachedTenderCheckpointStore(
        inner = inner,
        cache = newTtlCache<TenderCheckpoint>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender checkpoints. Empty fields match all. */
data class TenderCheckpointQuery(
    var title: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderCheckpoint): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TenderCheckpoint>): MutableList<TenderCheckpoint> {
        val matched = filter(items) { candidate: TenderCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderCheckpoints(store: TenderCheckpointStore, query: TenderCheckpointQuery): MutableList<TenderCheckpoint> {
    return query.apply(store.list())
}
