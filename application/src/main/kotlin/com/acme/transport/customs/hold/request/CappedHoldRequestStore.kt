package com.acme.transport.customs.hold.request

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped hold requests by code. */
interface CappedHoldRequestStore {
    fun find(code: String): CappedHoldRequest?
    fun save(item: CappedHoldRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedHoldRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedHoldRequestStore(
    var items: MutableMap<String, CappedHoldRequest>,
    var order: MutableList<String>,
) : CappedHoldRequestStore {
    override fun find(code: String): CappedHoldRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedHoldRequest) {
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

    override fun list(): MutableList<CappedHoldRequest> {
        val out = mutableListOf<CappedHoldRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedHoldRequestStore(): CappedHoldRequestStore {
    return MemoryCappedHoldRequestStore(mutableMapOf<String, CappedHoldRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedHoldRequestStore(
    var inner: CappedHoldRequestStore,
    var cache: TtlCache<CappedHoldRequest>,
    var hits: Int,
    var misses: Int,
) : CappedHoldRequestStore {
    override fun find(code: String): CappedHoldRequest? {
        val hit = (cache.get(code) as CappedHoldRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedHoldRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedHoldRequest> = inner.list()
}

fun newCachedCappedHoldRequestStore(inner: CappedHoldRequestStore, ttl: Int, clock: Clock): CappedHoldRequestStore {
    return CachedCappedHoldRequestStore(
        inner = inner,
        cache = newTtlCache<CappedHoldRequest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped hold requests. Empty fields match all. */
data class CappedHoldRequestQuery(
    var depot: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedHoldRequest): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedHoldRequest>): MutableList<CappedHoldRequest> {
        val matched = filter(items) { candidate: CappedHoldRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedHoldRequests(store: CappedHoldRequestStore, query: CappedHoldRequestQuery): MutableList<CappedHoldRequest> {
    return query.apply(store.list())
}
