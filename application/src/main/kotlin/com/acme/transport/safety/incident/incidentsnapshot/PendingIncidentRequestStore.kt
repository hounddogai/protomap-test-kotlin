package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps pending incident requests by code. */
interface PendingIncidentRequestStore {
    fun find(code: String): PendingIncidentRequest?
    fun save(item: PendingIncidentRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingIncidentRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingIncidentRequestStore(
    var items: MutableMap<String, PendingIncidentRequest>,
    var order: MutableList<String>,
) : PendingIncidentRequestStore {
    override fun find(code: String): PendingIncidentRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingIncidentRequest) {
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

    override fun list(): MutableList<PendingIncidentRequest> {
        val out = mutableListOf<PendingIncidentRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingIncidentRequestStore(): PendingIncidentRequestStore {
    return MemoryPendingIncidentRequestStore(
        mutableMapOf<String, PendingIncidentRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingIncidentRequestStore(
    var inner: PendingIncidentRequestStore,
    var cache: TtlCache<PendingIncidentRequest>,
    var hits: Int,
    var misses: Int,
) : PendingIncidentRequestStore {
    override fun find(code: String): PendingIncidentRequest? {
        val hit = (cache.get(code) as PendingIncidentRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingIncidentRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingIncidentRequest> = inner.list()
}

fun newCachedPendingIncidentRequestStore(inner: PendingIncidentRequestStore, ttl: Int, clock: Clock): PendingIncidentRequestStore {
    return CachedPendingIncidentRequestStore(
        inner = inner,
        cache = newTtlCache<PendingIncidentRequest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending incident requests. Empty fields match all. */
data class PendingIncidentRequestQuery(
    var country: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingIncidentRequest): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingIncidentRequest>): MutableList<PendingIncidentRequest> {
        val matched = filter(items) { candidate: PendingIncidentRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingIncidentRequests(store: PendingIncidentRequestStore, query: PendingIncidentRequestQuery): MutableList<PendingIncidentRequest> {
    return query.apply(store.list())
}
