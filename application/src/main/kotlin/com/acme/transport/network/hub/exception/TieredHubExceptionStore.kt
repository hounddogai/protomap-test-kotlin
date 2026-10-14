package com.acme.transport.network.hub.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps tiered hub exceptions by code. */
interface TieredHubExceptionStore {
    fun find(code: String): TieredHubException?
    fun save(item: TieredHubException)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredHubException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredHubExceptionStore(
    var items: MutableMap<String, TieredHubException>,
    var order: MutableList<String>,
) : TieredHubExceptionStore {
    override fun find(code: String): TieredHubException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredHubException) {
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

    override fun list(): MutableList<TieredHubException> {
        val out = mutableListOf<TieredHubException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredHubExceptionStore(): TieredHubExceptionStore {
    return MemoryTieredHubExceptionStore(mutableMapOf<String, TieredHubException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredHubExceptionStore(
    var inner: TieredHubExceptionStore,
    var cache: TtlCache<TieredHubException>,
    var hits: Int,
    var misses: Int,
) : TieredHubExceptionStore {
    override fun find(code: String): TieredHubException? {
        val hit = (cache.get(code) as TieredHubException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredHubException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredHubException> = inner.list()
}

fun newCachedTieredHubExceptionStore(inner: TieredHubExceptionStore, ttl: Int, clock: Clock): TieredHubExceptionStore {
    return CachedTieredHubExceptionStore(
        inner = inner,
        cache = newTtlCache<TieredHubException>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered hub exceptions. Empty fields match all. */
data class TieredHubExceptionQuery(
    var lane: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredHubException): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredHubException>): MutableList<TieredHubException> {
        val matched = filter(items) { candidate: TieredHubException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredHubExceptions(store: TieredHubExceptionStore, query: TieredHubExceptionQuery): MutableList<TieredHubException> {
    return query.apply(store.list())
}
