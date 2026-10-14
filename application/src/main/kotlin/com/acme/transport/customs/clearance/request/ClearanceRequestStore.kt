package com.acme.transport.customs.clearance.request

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance requests by code. */
interface ClearanceRequestStore {
    fun find(code: String): ClearanceRequest?
    fun save(item: ClearanceRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceRequestStore(
    var items: MutableMap<String, ClearanceRequest>,
    var order: MutableList<String>,
) : ClearanceRequestStore {
    override fun find(code: String): ClearanceRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceRequest) {
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

    override fun list(): MutableList<ClearanceRequest> {
        val out = mutableListOf<ClearanceRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceRequestStore(): ClearanceRequestStore {
    return MemoryClearanceRequestStore(mutableMapOf<String, ClearanceRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceRequestStore(
    var inner: ClearanceRequestStore,
    var cache: TtlCache<ClearanceRequest>,
    var hits: Int,
    var misses: Int,
) : ClearanceRequestStore {
    override fun find(code: String): ClearanceRequest? {
        val hit = (cache.get(code) as ClearanceRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceRequest> = inner.list()
}

fun newCachedClearanceRequestStore(inner: ClearanceRequestStore, ttl: Int, clock: Clock): ClearanceRequestStore {
    return CachedClearanceRequestStore(
        inner = inner,
        cache = newTtlCache<ClearanceRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance requests. Empty fields match all. */
data class ClearanceRequestQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceRequest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceRequest>): MutableList<ClearanceRequest> {
        val matched = filter(items) { candidate: ClearanceRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceRequests(store: ClearanceRequestStore, query: ClearanceRequestQuery): MutableList<ClearanceRequest> {
    return query.apply(store.list())
}
