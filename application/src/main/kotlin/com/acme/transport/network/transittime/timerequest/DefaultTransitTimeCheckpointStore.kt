package com.acme.transport.network.transittime.timerequest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default transit time checkpoints by code. */
interface DefaultTransitTimeCheckpointStore {
    fun find(code: String): DefaultTransitTimeCheckpoint?
    fun save(item: DefaultTransitTimeCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultTransitTimeCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultTransitTimeCheckpointStore(
    var items: MutableMap<String, DefaultTransitTimeCheckpoint>,
    var order: MutableList<String>,
) : DefaultTransitTimeCheckpointStore {
    override fun find(code: String): DefaultTransitTimeCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultTransitTimeCheckpoint) {
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

    override fun list(): MutableList<DefaultTransitTimeCheckpoint> {
        val out = mutableListOf<DefaultTransitTimeCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultTransitTimeCheckpointStore(): DefaultTransitTimeCheckpointStore {
    return MemoryDefaultTransitTimeCheckpointStore(
        mutableMapOf<String, DefaultTransitTimeCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultTransitTimeCheckpointStore(
    var inner: DefaultTransitTimeCheckpointStore,
    var cache: TtlCache<DefaultTransitTimeCheckpoint>,
    var hits: Int,
    var misses: Int,
) : DefaultTransitTimeCheckpointStore {
    override fun find(code: String): DefaultTransitTimeCheckpoint? {
        val hit = (cache.get(code) as DefaultTransitTimeCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultTransitTimeCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultTransitTimeCheckpoint> = inner.list()
}

fun newCachedDefaultTransitTimeCheckpointStore(inner: DefaultTransitTimeCheckpointStore, ttl: Int, clock: Clock): DefaultTransitTimeCheckpointStore {
    return CachedDefaultTransitTimeCheckpointStore(
        inner = inner,
        cache = newTtlCache<DefaultTransitTimeCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default transit time checkpoints. Empty fields match all. */
data class DefaultTransitTimeCheckpointQuery(
    var locale: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultTransitTimeCheckpoint): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultTransitTimeCheckpoint>): MutableList<DefaultTransitTimeCheckpoint> {
        val matched = filter(items) { candidate: DefaultTransitTimeCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultTransitTimeCheckpoints(store: DefaultTransitTimeCheckpointStore, query: DefaultTransitTimeCheckpointQuery): MutableList<DefaultTransitTimeCheckpoint> {
    return query.apply(store.list())
}
