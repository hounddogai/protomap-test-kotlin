package com.acme.transport.terminal.traileryard.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps default trailer yard checkpoints by code. */
interface DefaultTrailerYardCheckpointStore {
    fun find(code: String): DefaultTrailerYardCheckpoint?
    fun save(item: DefaultTrailerYardCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultTrailerYardCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultTrailerYardCheckpointStore(
    var items: MutableMap<String, DefaultTrailerYardCheckpoint>,
    var order: MutableList<String>,
) : DefaultTrailerYardCheckpointStore {
    override fun find(code: String): DefaultTrailerYardCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultTrailerYardCheckpoint) {
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

    override fun list(): MutableList<DefaultTrailerYardCheckpoint> {
        val out = mutableListOf<DefaultTrailerYardCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultTrailerYardCheckpointStore(): DefaultTrailerYardCheckpointStore {
    return MemoryDefaultTrailerYardCheckpointStore(
        mutableMapOf<String, DefaultTrailerYardCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultTrailerYardCheckpointStore(
    var inner: DefaultTrailerYardCheckpointStore,
    var cache: TtlCache<DefaultTrailerYardCheckpoint>,
    var hits: Int,
    var misses: Int,
) : DefaultTrailerYardCheckpointStore {
    override fun find(code: String): DefaultTrailerYardCheckpoint? {
        val hit = (cache.get(code) as DefaultTrailerYardCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultTrailerYardCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultTrailerYardCheckpoint> = inner.list()
}

fun newCachedDefaultTrailerYardCheckpointStore(inner: DefaultTrailerYardCheckpointStore, ttl: Int, clock: Clock): DefaultTrailerYardCheckpointStore {
    return CachedDefaultTrailerYardCheckpointStore(
        inner = inner,
        cache = newTtlCache<DefaultTrailerYardCheckpoint>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default trailer yard checkpoints. Empty fields match all. */
data class DefaultTrailerYardCheckpointQuery(
    var locale: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultTrailerYardCheckpoint): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultTrailerYardCheckpoint>): MutableList<DefaultTrailerYardCheckpoint> {
        val matched = filter(items) { candidate: DefaultTrailerYardCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultTrailerYardCheckpoints(store: DefaultTrailerYardCheckpointStore, query: DefaultTrailerYardCheckpointQuery): MutableList<DefaultTrailerYardCheckpoint> {
    return query.apply(store.list())
}
