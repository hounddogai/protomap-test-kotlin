package com.acme.transport.lastmile.drop.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop requests by code. */
interface DropRequestStore {
    fun find(code: String): DropRequest?
    fun save(item: DropRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropRequestStore(
    var items: MutableMap<String, DropRequest>,
    var order: MutableList<String>,
) : DropRequestStore {
    override fun find(code: String): DropRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropRequest) {
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

    override fun list(): MutableList<DropRequest> {
        val out = mutableListOf<DropRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropRequestStore(): DropRequestStore {
    return MemoryDropRequestStore(mutableMapOf<String, DropRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropRequestStore(
    var inner: DropRequestStore,
    var cache: TtlCache<DropRequest>,
    var hits: Int,
    var misses: Int,
) : DropRequestStore {
    override fun find(code: String): DropRequest? {
        val hit = (cache.get(code) as DropRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropRequest> = inner.list()
}

fun newCachedDropRequestStore(inner: DropRequestStore, ttl: Int, clock: Clock): DropRequestStore {
    return CachedDropRequestStore(
        inner = inner,
        cache = newTtlCache<DropRequest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop requests. Empty fields match all. */
data class DropRequestQuery(
    var lane: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropRequest): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropRequest>): MutableList<DropRequest> {
        val matched = filter(items) { candidate: DropRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropRequests(store: DropRequestStore, query: DropRequestQuery): MutableList<DropRequest> {
    return query.apply(store.list())
}
