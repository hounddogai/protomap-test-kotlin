package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps capped hub requests by code. */
interface CappedHubRequestStore {
    fun find(code: String): CappedHubRequest?
    fun save(item: CappedHubRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedHubRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedHubRequestStore(
    var items: MutableMap<String, CappedHubRequest>,
    var order: MutableList<String>,
) : CappedHubRequestStore {
    override fun find(code: String): CappedHubRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedHubRequest) {
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

    override fun list(): MutableList<CappedHubRequest> {
        val out = mutableListOf<CappedHubRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedHubRequestStore(): CappedHubRequestStore {
    return MemoryCappedHubRequestStore(mutableMapOf<String, CappedHubRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedHubRequestStore(
    var inner: CappedHubRequestStore,
    var cache: TtlCache<CappedHubRequest>,
    var hits: Int,
    var misses: Int,
) : CappedHubRequestStore {
    override fun find(code: String): CappedHubRequest? {
        val hit = (cache.get(code) as CappedHubRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedHubRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedHubRequest> = inner.list()
}

fun newCachedCappedHubRequestStore(inner: CappedHubRequestStore, ttl: Int, clock: Clock): CappedHubRequestStore {
    return CachedCappedHubRequestStore(
        inner = inner,
        cache = newTtlCache<CappedHubRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped hub requests. Empty fields match all. */
data class CappedHubRequestQuery(
    var label: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedHubRequest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedHubRequest>): MutableList<CappedHubRequest> {
        val matched = filter(items) { candidate: CappedHubRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedHubRequests(store: CappedHubRequestStore, query: CappedHubRequestQuery): MutableList<CappedHubRequest> {
    return query.apply(store.list())
}
