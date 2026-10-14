package com.acme.transport.lastmile.attempt.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps pending attempt requests by code. */
interface PendingAttemptRequestStore {
    fun find(code: String): PendingAttemptRequest?
    fun save(item: PendingAttemptRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingAttemptRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingAttemptRequestStore(
    var items: MutableMap<String, PendingAttemptRequest>,
    var order: MutableList<String>,
) : PendingAttemptRequestStore {
    override fun find(code: String): PendingAttemptRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingAttemptRequest) {
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

    override fun list(): MutableList<PendingAttemptRequest> {
        val out = mutableListOf<PendingAttemptRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingAttemptRequestStore(): PendingAttemptRequestStore {
    return MemoryPendingAttemptRequestStore(
        mutableMapOf<String, PendingAttemptRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingAttemptRequestStore(
    var inner: PendingAttemptRequestStore,
    var cache: TtlCache<PendingAttemptRequest>,
    var hits: Int,
    var misses: Int,
) : PendingAttemptRequestStore {
    override fun find(code: String): PendingAttemptRequest? {
        val hit = (cache.get(code) as PendingAttemptRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingAttemptRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingAttemptRequest> = inner.list()
}

fun newCachedPendingAttemptRequestStore(inner: PendingAttemptRequestStore, ttl: Int, clock: Clock): PendingAttemptRequestStore {
    return CachedPendingAttemptRequestStore(
        inner = inner,
        cache = newTtlCache<PendingAttemptRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending attempt requests. Empty fields match all. */
data class PendingAttemptRequestQuery(
    var label: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingAttemptRequest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingAttemptRequest>): MutableList<PendingAttemptRequest> {
        val matched = filter(items) { candidate: PendingAttemptRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingAttemptRequests(store: PendingAttemptRequestStore, query: PendingAttemptRequestQuery): MutableList<PendingAttemptRequest> {
    return query.apply(store.list())
}
