package com.acme.transport.safety.coaching.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching requests by code. */
interface CoachingRequestStore {
    fun find(code: String): CoachingRequest?
    fun save(item: CoachingRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingRequestStore(
    var items: MutableMap<String, CoachingRequest>,
    var order: MutableList<String>,
) : CoachingRequestStore {
    override fun find(code: String): CoachingRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingRequest) {
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

    override fun list(): MutableList<CoachingRequest> {
        val out = mutableListOf<CoachingRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingRequestStore(): CoachingRequestStore {
    return MemoryCoachingRequestStore(mutableMapOf<String, CoachingRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingRequestStore(
    var inner: CoachingRequestStore,
    var cache: TtlCache<CoachingRequest>,
    var hits: Int,
    var misses: Int,
) : CoachingRequestStore {
    override fun find(code: String): CoachingRequest? {
        val hit = (cache.get(code) as CoachingRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingRequest> = inner.list()
}

fun newCachedCoachingRequestStore(inner: CoachingRequestStore, ttl: Int, clock: Clock): CoachingRequestStore {
    return CachedCoachingRequestStore(
        inner = inner,
        cache = newTtlCache<CoachingRequest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching requests. Empty fields match all. */
data class CoachingRequestQuery(
    var channel: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingRequest): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingRequest>): MutableList<CoachingRequest> {
        val matched = filter(items) { candidate: CoachingRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingRequests(store: CoachingRequestStore, query: CoachingRequestQuery): MutableList<CoachingRequest> {
    return query.apply(store.list())
}
