package com.acme.transport.lastmile.attempt.attemptrequest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps lenient attempt requests by code. */
interface LenientAttemptRequestStore {
    fun find(code: String): LenientAttemptRequest?
    fun save(item: LenientAttemptRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientAttemptRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientAttemptRequestStore(
    var items: MutableMap<String, LenientAttemptRequest>,
    var order: MutableList<String>,
) : LenientAttemptRequestStore {
    override fun find(code: String): LenientAttemptRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientAttemptRequest) {
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

    override fun list(): MutableList<LenientAttemptRequest> {
        val out = mutableListOf<LenientAttemptRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientAttemptRequestStore(): LenientAttemptRequestStore {
    return MemoryLenientAttemptRequestStore(
        mutableMapOf<String, LenientAttemptRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientAttemptRequestStore(
    var inner: LenientAttemptRequestStore,
    var cache: TtlCache<LenientAttemptRequest>,
    var hits: Int,
    var misses: Int,
) : LenientAttemptRequestStore {
    override fun find(code: String): LenientAttemptRequest? {
        val hit = (cache.get(code) as LenientAttemptRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientAttemptRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientAttemptRequest> = inner.list()
}

fun newCachedLenientAttemptRequestStore(inner: LenientAttemptRequestStore, ttl: Int, clock: Clock): LenientAttemptRequestStore {
    return CachedLenientAttemptRequestStore(
        inner = inner,
        cache = newTtlCache<LenientAttemptRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient attempt requests. Empty fields match all. */
data class LenientAttemptRequestQuery(
    var source: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientAttemptRequest): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientAttemptRequest>): MutableList<LenientAttemptRequest> {
        val matched = filter(items) { candidate: LenientAttemptRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientAttemptRequests(store: LenientAttemptRequestStore, query: LenientAttemptRequestQuery): MutableList<LenientAttemptRequest> {
    return query.apply(store.list())
}
