package com.acme.transport.linehaul.capacity.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity requests by code. */
interface CapacityRequestStore {
    fun find(code: String): CapacityRequest?
    fun save(item: CapacityRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityRequestStore(
    var items: MutableMap<String, CapacityRequest>,
    var order: MutableList<String>,
) : CapacityRequestStore {
    override fun find(code: String): CapacityRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityRequest) {
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

    override fun list(): MutableList<CapacityRequest> {
        val out = mutableListOf<CapacityRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityRequestStore(): CapacityRequestStore {
    return MemoryCapacityRequestStore(mutableMapOf<String, CapacityRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityRequestStore(
    var inner: CapacityRequestStore,
    var cache: TtlCache<CapacityRequest>,
    var hits: Int,
    var misses: Int,
) : CapacityRequestStore {
    override fun find(code: String): CapacityRequest? {
        val hit = (cache.get(code) as CapacityRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityRequest> = inner.list()
}

fun newCachedCapacityRequestStore(inner: CapacityRequestStore, ttl: Int, clock: Clock): CapacityRequestStore {
    return CachedCapacityRequestStore(
        inner = inner,
        cache = newTtlCache<CapacityRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity requests. Empty fields match all. */
data class CapacityRequestQuery(
    var category: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityRequest): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityRequest>): MutableList<CapacityRequest> {
        val matched = filter(items) { candidate: CapacityRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityRequests(store: CapacityRequestStore, query: CapacityRequestQuery): MutableList<CapacityRequest> {
    return query.apply(store.list())
}
