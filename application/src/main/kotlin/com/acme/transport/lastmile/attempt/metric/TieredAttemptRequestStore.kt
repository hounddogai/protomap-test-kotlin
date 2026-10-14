package com.acme.transport.lastmile.attempt.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps tiered attempt requests by code. */
interface TieredAttemptRequestStore {
    fun find(code: String): TieredAttemptRequest?
    fun save(item: TieredAttemptRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredAttemptRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredAttemptRequestStore(
    var items: MutableMap<String, TieredAttemptRequest>,
    var order: MutableList<String>,
) : TieredAttemptRequestStore {
    override fun find(code: String): TieredAttemptRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredAttemptRequest) {
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

    override fun list(): MutableList<TieredAttemptRequest> {
        val out = mutableListOf<TieredAttemptRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredAttemptRequestStore(): TieredAttemptRequestStore {
    return MemoryTieredAttemptRequestStore(
        mutableMapOf<String, TieredAttemptRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredAttemptRequestStore(
    var inner: TieredAttemptRequestStore,
    var cache: TtlCache<TieredAttemptRequest>,
    var hits: Int,
    var misses: Int,
) : TieredAttemptRequestStore {
    override fun find(code: String): TieredAttemptRequest? {
        val hit = (cache.get(code) as TieredAttemptRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredAttemptRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredAttemptRequest> = inner.list()
}

fun newCachedTieredAttemptRequestStore(inner: TieredAttemptRequestStore, ttl: Int, clock: Clock): TieredAttemptRequestStore {
    return CachedTieredAttemptRequestStore(
        inner = inner,
        cache = newTtlCache<TieredAttemptRequest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered attempt requests. Empty fields match all. */
data class TieredAttemptRequestQuery(
    var label: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredAttemptRequest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredAttemptRequest>): MutableList<TieredAttemptRequest> {
        val matched = filter(items) { candidate: TieredAttemptRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredAttemptRequests(store: TieredAttemptRequestStore, query: TieredAttemptRequestQuery): MutableList<TieredAttemptRequest> {
    return query.apply(store.list())
}
