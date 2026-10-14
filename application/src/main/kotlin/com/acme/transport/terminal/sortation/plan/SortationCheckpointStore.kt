package com.acme.transport.terminal.sortation.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation checkpoints by code. */
interface SortationCheckpointStore {
    fun find(code: String): SortationCheckpoint?
    fun save(item: SortationCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationCheckpointStore(
    var items: MutableMap<String, SortationCheckpoint>,
    var order: MutableList<String>,
) : SortationCheckpointStore {
    override fun find(code: String): SortationCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationCheckpoint) {
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

    override fun list(): MutableList<SortationCheckpoint> {
        val out = mutableListOf<SortationCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationCheckpointStore(): SortationCheckpointStore {
    return MemorySortationCheckpointStore(mutableMapOf<String, SortationCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationCheckpointStore(
    var inner: SortationCheckpointStore,
    var cache: TtlCache<SortationCheckpoint>,
    var hits: Int,
    var misses: Int,
) : SortationCheckpointStore {
    override fun find(code: String): SortationCheckpoint? {
        val hit = (cache.get(code) as SortationCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationCheckpoint> = inner.list()
}

fun newCachedSortationCheckpointStore(inner: SortationCheckpointStore, ttl: Int, clock: Clock): SortationCheckpointStore {
    return CachedSortationCheckpointStore(
        inner = inner,
        cache = newTtlCache<SortationCheckpoint>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation checkpoints. Empty fields match all. */
data class SortationCheckpointQuery(
    var locale: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationCheckpoint): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<SortationCheckpoint>): MutableList<SortationCheckpoint> {
        val matched = filter(items) { candidate: SortationCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationCheckpoints(store: SortationCheckpointStore, query: SortationCheckpointQuery): MutableList<SortationCheckpoint> {
    return query.apply(store.list())
}
