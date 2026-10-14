package com.acme.transport.crewing.swap.bundle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default swap checkpoints by code. */
interface DefaultSwapCheckpointStore {
    fun find(code: String): DefaultSwapCheckpoint?
    fun save(item: DefaultSwapCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultSwapCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultSwapCheckpointStore(
    var items: MutableMap<String, DefaultSwapCheckpoint>,
    var order: MutableList<String>,
) : DefaultSwapCheckpointStore {
    override fun find(code: String): DefaultSwapCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultSwapCheckpoint) {
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

    override fun list(): MutableList<DefaultSwapCheckpoint> {
        val out = mutableListOf<DefaultSwapCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultSwapCheckpointStore(): DefaultSwapCheckpointStore {
    return MemoryDefaultSwapCheckpointStore(
        mutableMapOf<String, DefaultSwapCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultSwapCheckpointStore(
    var inner: DefaultSwapCheckpointStore,
    var cache: TtlCache<DefaultSwapCheckpoint>,
    var hits: Int,
    var misses: Int,
) : DefaultSwapCheckpointStore {
    override fun find(code: String): DefaultSwapCheckpoint? {
        val hit = (cache.get(code) as DefaultSwapCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultSwapCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultSwapCheckpoint> = inner.list()
}

fun newCachedDefaultSwapCheckpointStore(inner: DefaultSwapCheckpointStore, ttl: Int, clock: Clock): DefaultSwapCheckpointStore {
    return CachedDefaultSwapCheckpointStore(
        inner = inner,
        cache = newTtlCache<DefaultSwapCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default swap checkpoints. Empty fields match all. */
data class DefaultSwapCheckpointQuery(
    var source: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultSwapCheckpoint): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultSwapCheckpoint>): MutableList<DefaultSwapCheckpoint> {
        val matched = filter(items) { candidate: DefaultSwapCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultSwapCheckpoints(store: DefaultSwapCheckpointStore, query: DefaultSwapCheckpointQuery): MutableList<DefaultSwapCheckpoint> {
    return query.apply(store.list())
}
