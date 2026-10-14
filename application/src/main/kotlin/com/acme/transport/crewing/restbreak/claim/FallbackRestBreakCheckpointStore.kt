package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback rest break checkpoints by code. */
interface FallbackRestBreakCheckpointStore {
    fun find(code: String): FallbackRestBreakCheckpoint?
    fun save(item: FallbackRestBreakCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRestBreakCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRestBreakCheckpointStore(
    var items: MutableMap<String, FallbackRestBreakCheckpoint>,
    var order: MutableList<String>,
) : FallbackRestBreakCheckpointStore {
    override fun find(code: String): FallbackRestBreakCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRestBreakCheckpoint) {
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

    override fun list(): MutableList<FallbackRestBreakCheckpoint> {
        val out = mutableListOf<FallbackRestBreakCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRestBreakCheckpointStore(): FallbackRestBreakCheckpointStore {
    return MemoryFallbackRestBreakCheckpointStore(
        mutableMapOf<String, FallbackRestBreakCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRestBreakCheckpointStore(
    var inner: FallbackRestBreakCheckpointStore,
    var cache: TtlCache<FallbackRestBreakCheckpoint>,
    var hits: Int,
    var misses: Int,
) : FallbackRestBreakCheckpointStore {
    override fun find(code: String): FallbackRestBreakCheckpoint? {
        val hit = (cache.get(code) as FallbackRestBreakCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRestBreakCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRestBreakCheckpoint> = inner.list()
}

fun newCachedFallbackRestBreakCheckpointStore(inner: FallbackRestBreakCheckpointStore, ttl: Int, clock: Clock): FallbackRestBreakCheckpointStore {
    return CachedFallbackRestBreakCheckpointStore(
        inner = inner,
        cache = newTtlCache<FallbackRestBreakCheckpoint>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback rest break checkpoints. Empty fields match all. */
data class FallbackRestBreakCheckpointQuery(
    var locale: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRestBreakCheckpoint): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackRestBreakCheckpoint>): MutableList<FallbackRestBreakCheckpoint> {
        val matched = filter(items) { candidate: FallbackRestBreakCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRestBreakCheckpoints(store: FallbackRestBreakCheckpointStore, query: FallbackRestBreakCheckpointQuery): MutableList<FallbackRestBreakCheckpoint> {
    return query.apply(store.list())
}
