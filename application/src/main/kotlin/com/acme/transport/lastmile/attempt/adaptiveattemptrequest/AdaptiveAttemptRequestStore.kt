package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps adaptive attempt requests by code. */
interface AdaptiveAttemptRequestStore {
    fun find(code: String): AdaptiveAttemptRequest?
    fun save(item: AdaptiveAttemptRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveAttemptRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveAttemptRequestStore(
    var items: MutableMap<String, AdaptiveAttemptRequest>,
    var order: MutableList<String>,
) : AdaptiveAttemptRequestStore {
    override fun find(code: String): AdaptiveAttemptRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveAttemptRequest) {
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

    override fun list(): MutableList<AdaptiveAttemptRequest> {
        val out = mutableListOf<AdaptiveAttemptRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveAttemptRequestStore(): AdaptiveAttemptRequestStore {
    return MemoryAdaptiveAttemptRequestStore(
        mutableMapOf<String, AdaptiveAttemptRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveAttemptRequestStore(
    var inner: AdaptiveAttemptRequestStore,
    var cache: TtlCache<AdaptiveAttemptRequest>,
    var hits: Int,
    var misses: Int,
) : AdaptiveAttemptRequestStore {
    override fun find(code: String): AdaptiveAttemptRequest? {
        val hit = (cache.get(code) as AdaptiveAttemptRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveAttemptRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveAttemptRequest> = inner.list()
}

fun newCachedAdaptiveAttemptRequestStore(inner: AdaptiveAttemptRequestStore, ttl: Int, clock: Clock): AdaptiveAttemptRequestStore {
    return CachedAdaptiveAttemptRequestStore(
        inner = inner,
        cache = newTtlCache<AdaptiveAttemptRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive attempt requests. Empty fields match all. */
data class AdaptiveAttemptRequestQuery(
    var label: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveAttemptRequest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveAttemptRequest>): MutableList<AdaptiveAttemptRequest> {
        val matched = filter(items) { candidate: AdaptiveAttemptRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveAttemptRequests(store: AdaptiveAttemptRequestStore, query: AdaptiveAttemptRequestQuery): MutableList<AdaptiveAttemptRequest> {
    return query.apply(store.list())
}
