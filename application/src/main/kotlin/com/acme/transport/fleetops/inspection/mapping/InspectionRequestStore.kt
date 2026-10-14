package com.acme.transport.fleetops.inspection.mapping

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection requests by code. */
interface InspectionRequestStore {
    fun find(code: String): InspectionRequest?
    fun save(item: InspectionRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionRequestStore(
    var items: MutableMap<String, InspectionRequest>,
    var order: MutableList<String>,
) : InspectionRequestStore {
    override fun find(code: String): InspectionRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionRequest) {
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

    override fun list(): MutableList<InspectionRequest> {
        val out = mutableListOf<InspectionRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionRequestStore(): InspectionRequestStore {
    return MemoryInspectionRequestStore(mutableMapOf<String, InspectionRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionRequestStore(
    var inner: InspectionRequestStore,
    var cache: TtlCache<InspectionRequest>,
    var hits: Int,
    var misses: Int,
) : InspectionRequestStore {
    override fun find(code: String): InspectionRequest? {
        val hit = (cache.get(code) as InspectionRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionRequest> = inner.list()
}

fun newCachedInspectionRequestStore(inner: InspectionRequestStore, ttl: Int, clock: Clock): InspectionRequestStore {
    return CachedInspectionRequestStore(
        inner = inner,
        cache = newTtlCache<InspectionRequest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection requests. Empty fields match all. */
data class InspectionRequestQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionRequest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<InspectionRequest>): MutableList<InspectionRequest> {
        val matched = filter(items) { candidate: InspectionRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionRequests(store: InspectionRequestStore, query: InspectionRequestQuery): MutableList<InspectionRequest> {
    return query.apply(store.list())
}
