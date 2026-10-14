package com.acme.transport.terminal.dwell.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell checkpoints by code. */
interface DwellCheckpointStore {
    fun find(code: String): DwellCheckpoint?
    fun save(item: DwellCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellCheckpointStore(
    var items: MutableMap<String, DwellCheckpoint>,
    var order: MutableList<String>,
) : DwellCheckpointStore {
    override fun find(code: String): DwellCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellCheckpoint) {
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

    override fun list(): MutableList<DwellCheckpoint> {
        val out = mutableListOf<DwellCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellCheckpointStore(): DwellCheckpointStore {
    return MemoryDwellCheckpointStore(mutableMapOf<String, DwellCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellCheckpointStore(
    var inner: DwellCheckpointStore,
    var cache: TtlCache<DwellCheckpoint>,
    var hits: Int,
    var misses: Int,
) : DwellCheckpointStore {
    override fun find(code: String): DwellCheckpoint? {
        val hit = (cache.get(code) as DwellCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellCheckpoint> = inner.list()
}

fun newCachedDwellCheckpointStore(inner: DwellCheckpointStore, ttl: Int, clock: Clock): DwellCheckpointStore {
    return CachedDwellCheckpointStore(
        inner = inner,
        cache = newTtlCache<DwellCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell checkpoints. Empty fields match all. */
data class DwellCheckpointQuery(
    var label: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellCheckpoint): Boolean {
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

    fun apply(items: MutableList<DwellCheckpoint>): MutableList<DwellCheckpoint> {
        val matched = filter(items) { candidate: DwellCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellCheckpoints(store: DwellCheckpointStore, query: DwellCheckpointQuery): MutableList<DwellCheckpoint> {
    return query.apply(store.list())
}
