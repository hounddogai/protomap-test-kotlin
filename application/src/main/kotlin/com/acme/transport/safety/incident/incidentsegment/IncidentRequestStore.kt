package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident requests by code. */
interface IncidentRequestStore {
    fun find(code: String): IncidentRequest?
    fun save(item: IncidentRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentRequestStore(
    var items: MutableMap<String, IncidentRequest>,
    var order: MutableList<String>,
) : IncidentRequestStore {
    override fun find(code: String): IncidentRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentRequest) {
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

    override fun list(): MutableList<IncidentRequest> {
        val out = mutableListOf<IncidentRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentRequestStore(): IncidentRequestStore {
    return MemoryIncidentRequestStore(mutableMapOf<String, IncidentRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentRequestStore(
    var inner: IncidentRequestStore,
    var cache: TtlCache<IncidentRequest>,
    var hits: Int,
    var misses: Int,
) : IncidentRequestStore {
    override fun find(code: String): IncidentRequest? {
        val hit = (cache.get(code) as IncidentRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentRequest> = inner.list()
}

fun newCachedIncidentRequestStore(inner: IncidentRequestStore, ttl: Int, clock: Clock): IncidentRequestStore {
    return CachedIncidentRequestStore(
        inner = inner,
        cache = newTtlCache<IncidentRequest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident requests. Empty fields match all. */
data class IncidentRequestQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentRequest): Boolean {
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

    fun apply(items: MutableList<IncidentRequest>): MutableList<IncidentRequest> {
        val matched = filter(items) { candidate: IncidentRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentRequests(store: IncidentRequestStore, query: IncidentRequestQuery): MutableList<IncidentRequest> {
    return query.apply(store.list())
}
