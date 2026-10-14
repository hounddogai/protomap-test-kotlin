package com.acme.transport.terminal.dwell.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps lenient dwell checkpoints by code. */
interface LenientDwellCheckpointStore {
    fun find(code: String): LenientDwellCheckpoint?
    fun save(item: LenientDwellCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDwellCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDwellCheckpointStore(
    var items: MutableMap<String, LenientDwellCheckpoint>,
    var order: MutableList<String>,
) : LenientDwellCheckpointStore {
    override fun find(code: String): LenientDwellCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDwellCheckpoint) {
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

    override fun list(): MutableList<LenientDwellCheckpoint> {
        val out = mutableListOf<LenientDwellCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDwellCheckpointStore(): LenientDwellCheckpointStore {
    return MemoryLenientDwellCheckpointStore(
        mutableMapOf<String, LenientDwellCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDwellCheckpointStore(
    var inner: LenientDwellCheckpointStore,
    var cache: TtlCache<LenientDwellCheckpoint>,
    var hits: Int,
    var misses: Int,
) : LenientDwellCheckpointStore {
    override fun find(code: String): LenientDwellCheckpoint? {
        val hit = (cache.get(code) as LenientDwellCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDwellCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDwellCheckpoint> = inner.list()
}

fun newCachedLenientDwellCheckpointStore(inner: LenientDwellCheckpointStore, ttl: Int, clock: Clock): LenientDwellCheckpointStore {
    return CachedLenientDwellCheckpointStore(
        inner = inner,
        cache = newTtlCache<LenientDwellCheckpoint>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient dwell checkpoints. Empty fields match all. */
data class LenientDwellCheckpointQuery(
    var label: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDwellCheckpoint): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientDwellCheckpoint>): MutableList<LenientDwellCheckpoint> {
        val matched = filter(items) { candidate: LenientDwellCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDwellCheckpoints(store: LenientDwellCheckpointStore, query: LenientDwellCheckpointQuery): MutableList<LenientDwellCheckpoint> {
    return query.apply(store.list())
}
