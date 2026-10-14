package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lenient tender checkpoints by code. */
interface LenientTenderCheckpointStore {
    fun find(code: String): LenientTenderCheckpoint?
    fun save(item: LenientTenderCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientTenderCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientTenderCheckpointStore(
    var items: MutableMap<String, LenientTenderCheckpoint>,
    var order: MutableList<String>,
) : LenientTenderCheckpointStore {
    override fun find(code: String): LenientTenderCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientTenderCheckpoint) {
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

    override fun list(): MutableList<LenientTenderCheckpoint> {
        val out = mutableListOf<LenientTenderCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientTenderCheckpointStore(): LenientTenderCheckpointStore {
    return MemoryLenientTenderCheckpointStore(
        mutableMapOf<String, LenientTenderCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientTenderCheckpointStore(
    var inner: LenientTenderCheckpointStore,
    var cache: TtlCache<LenientTenderCheckpoint>,
    var hits: Int,
    var misses: Int,
) : LenientTenderCheckpointStore {
    override fun find(code: String): LenientTenderCheckpoint? {
        val hit = (cache.get(code) as LenientTenderCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientTenderCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientTenderCheckpoint> = inner.list()
}

fun newCachedLenientTenderCheckpointStore(inner: LenientTenderCheckpointStore, ttl: Int, clock: Clock): LenientTenderCheckpointStore {
    return CachedLenientTenderCheckpointStore(
        inner = inner,
        cache = newTtlCache<LenientTenderCheckpoint>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient tender checkpoints. Empty fields match all. */
data class LenientTenderCheckpointQuery(
    var label: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientTenderCheckpoint): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientTenderCheckpoint>): MutableList<LenientTenderCheckpoint> {
        val matched = filter(items) { candidate: LenientTenderCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientTenderCheckpoints(store: LenientTenderCheckpointStore, query: LenientTenderCheckpointQuery): MutableList<LenientTenderCheckpoint> {
    return query.apply(store.list())
}
