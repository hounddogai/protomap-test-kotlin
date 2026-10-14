package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock requests by code. */
interface CrossDockRequestStore {
    fun find(code: String): CrossDockRequest?
    fun save(item: CrossDockRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockRequestStore(
    var items: MutableMap<String, CrossDockRequest>,
    var order: MutableList<String>,
) : CrossDockRequestStore {
    override fun find(code: String): CrossDockRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockRequest) {
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

    override fun list(): MutableList<CrossDockRequest> {
        val out = mutableListOf<CrossDockRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockRequestStore(): CrossDockRequestStore {
    return MemoryCrossDockRequestStore(mutableMapOf<String, CrossDockRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockRequestStore(
    var inner: CrossDockRequestStore,
    var cache: TtlCache<CrossDockRequest>,
    var hits: Int,
    var misses: Int,
) : CrossDockRequestStore {
    override fun find(code: String): CrossDockRequest? {
        val hit = (cache.get(code) as CrossDockRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockRequest> = inner.list()
}

fun newCachedCrossDockRequestStore(inner: CrossDockRequestStore, ttl: Int, clock: Clock): CrossDockRequestStore {
    return CachedCrossDockRequestStore(
        inner = inner,
        cache = newTtlCache<CrossDockRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock requests. Empty fields match all. */
data class CrossDockRequestQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockRequest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<CrossDockRequest>): MutableList<CrossDockRequest> {
        val matched = filter(items) { candidate: CrossDockRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockRequests(store: CrossDockRequestStore, query: CrossDockRequestQuery): MutableList<CrossDockRequest> {
    return query.apply(store.list())
}
