package com.acme.transport.safety.incident.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps global incident requests by code. */
interface GlobalIncidentRequestStore {
    fun find(code: String): GlobalIncidentRequest?
    fun save(item: GlobalIncidentRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalIncidentRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalIncidentRequestStore(
    var items: MutableMap<String, GlobalIncidentRequest>,
    var order: MutableList<String>,
) : GlobalIncidentRequestStore {
    override fun find(code: String): GlobalIncidentRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalIncidentRequest) {
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

    override fun list(): MutableList<GlobalIncidentRequest> {
        val out = mutableListOf<GlobalIncidentRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalIncidentRequestStore(): GlobalIncidentRequestStore {
    return MemoryGlobalIncidentRequestStore(
        mutableMapOf<String, GlobalIncidentRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalIncidentRequestStore(
    var inner: GlobalIncidentRequestStore,
    var cache: TtlCache<GlobalIncidentRequest>,
    var hits: Int,
    var misses: Int,
) : GlobalIncidentRequestStore {
    override fun find(code: String): GlobalIncidentRequest? {
        val hit = (cache.get(code) as GlobalIncidentRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalIncidentRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalIncidentRequest> = inner.list()
}

fun newCachedGlobalIncidentRequestStore(inner: GlobalIncidentRequestStore, ttl: Int, clock: Clock): GlobalIncidentRequestStore {
    return CachedGlobalIncidentRequestStore(
        inner = inner,
        cache = newTtlCache<GlobalIncidentRequest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global incident requests. Empty fields match all. */
data class GlobalIncidentRequestQuery(
    var title: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalIncidentRequest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalIncidentRequest>): MutableList<GlobalIncidentRequest> {
        val matched = filter(items) { candidate: GlobalIncidentRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalIncidentRequests(store: GlobalIncidentRequestStore, query: GlobalIncidentRequestQuery): MutableList<GlobalIncidentRequest> {
    return query.apply(store.list())
}
