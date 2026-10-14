package com.acme.transport.fleetops.inspection.score

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped inspection requests by code. */
interface CappedInspectionRequestStore {
    fun find(code: String): CappedInspectionRequest?
    fun save(item: CappedInspectionRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedInspectionRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedInspectionRequestStore(
    var items: MutableMap<String, CappedInspectionRequest>,
    var order: MutableList<String>,
) : CappedInspectionRequestStore {
    override fun find(code: String): CappedInspectionRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedInspectionRequest) {
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

    override fun list(): MutableList<CappedInspectionRequest> {
        val out = mutableListOf<CappedInspectionRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedInspectionRequestStore(): CappedInspectionRequestStore {
    return MemoryCappedInspectionRequestStore(
        mutableMapOf<String, CappedInspectionRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedInspectionRequestStore(
    var inner: CappedInspectionRequestStore,
    var cache: TtlCache<CappedInspectionRequest>,
    var hits: Int,
    var misses: Int,
) : CappedInspectionRequestStore {
    override fun find(code: String): CappedInspectionRequest? {
        val hit = (cache.get(code) as CappedInspectionRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedInspectionRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedInspectionRequest> = inner.list()
}

fun newCachedCappedInspectionRequestStore(inner: CappedInspectionRequestStore, ttl: Int, clock: Clock): CappedInspectionRequestStore {
    return CachedCappedInspectionRequestStore(
        inner = inner,
        cache = newTtlCache<CappedInspectionRequest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped inspection requests. Empty fields match all. */
data class CappedInspectionRequestQuery(
    var locale: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedInspectionRequest): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedInspectionRequest>): MutableList<CappedInspectionRequest> {
        val matched = filter(items) { candidate: CappedInspectionRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedInspectionRequests(store: CappedInspectionRequestStore, query: CappedInspectionRequestQuery): MutableList<CappedInspectionRequest> {
    return query.apply(store.list())
}
