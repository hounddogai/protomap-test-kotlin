package com.acme.transport.lastmile.attempt.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt requests by code. */
interface AttemptRequestStore {
    fun find(code: String): AttemptRequest?
    fun save(item: AttemptRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptRequestStore(
    var items: MutableMap<String, AttemptRequest>,
    var order: MutableList<String>,
) : AttemptRequestStore {
    override fun find(code: String): AttemptRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptRequest) {
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

    override fun list(): MutableList<AttemptRequest> {
        val out = mutableListOf<AttemptRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptRequestStore(): AttemptRequestStore {
    return MemoryAttemptRequestStore(mutableMapOf<String, AttemptRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptRequestStore(
    var inner: AttemptRequestStore,
    var cache: TtlCache<AttemptRequest>,
    var hits: Int,
    var misses: Int,
) : AttemptRequestStore {
    override fun find(code: String): AttemptRequest? {
        val hit = (cache.get(code) as AttemptRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptRequest> = inner.list()
}

fun newCachedAttemptRequestStore(inner: AttemptRequestStore, ttl: Int, clock: Clock): AttemptRequestStore {
    return CachedAttemptRequestStore(
        inner = inner,
        cache = newTtlCache<AttemptRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt requests. Empty fields match all. */
data class AttemptRequestQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptRequest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptRequest>): MutableList<AttemptRequest> {
        val matched = filter(items) { candidate: AttemptRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptRequests(store: AttemptRequestStore, query: AttemptRequestQuery): MutableList<AttemptRequest> {
    return query.apply(store.list())
}
