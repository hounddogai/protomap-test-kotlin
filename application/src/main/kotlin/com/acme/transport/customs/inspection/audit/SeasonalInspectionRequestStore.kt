package com.acme.transport.customs.inspection.audit

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal inspection requests by code. */
interface SeasonalInspectionRequestStore {
    fun find(code: String): SeasonalInspectionRequest?
    fun save(item: SeasonalInspectionRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalInspectionRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalInspectionRequestStore(
    var items: MutableMap<String, SeasonalInspectionRequest>,
    var order: MutableList<String>,
) : SeasonalInspectionRequestStore {
    override fun find(code: String): SeasonalInspectionRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalInspectionRequest) {
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

    override fun list(): MutableList<SeasonalInspectionRequest> {
        val out = mutableListOf<SeasonalInspectionRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalInspectionRequestStore(): SeasonalInspectionRequestStore {
    return MemorySeasonalInspectionRequestStore(
        mutableMapOf<String, SeasonalInspectionRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalInspectionRequestStore(
    var inner: SeasonalInspectionRequestStore,
    var cache: TtlCache<SeasonalInspectionRequest>,
    var hits: Int,
    var misses: Int,
) : SeasonalInspectionRequestStore {
    override fun find(code: String): SeasonalInspectionRequest? {
        val hit = (cache.get(code) as SeasonalInspectionRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalInspectionRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalInspectionRequest> = inner.list()
}

fun newCachedSeasonalInspectionRequestStore(inner: SeasonalInspectionRequestStore, ttl: Int, clock: Clock): SeasonalInspectionRequestStore {
    return CachedSeasonalInspectionRequestStore(
        inner = inner,
        cache = newTtlCache<SeasonalInspectionRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal inspection requests. Empty fields match all. */
data class SeasonalInspectionRequestQuery(
    var region: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalInspectionRequest): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalInspectionRequest>): MutableList<SeasonalInspectionRequest> {
        val matched = filter(items) { candidate: SeasonalInspectionRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalInspectionRequests(store: SeasonalInspectionRequestStore, query: SeasonalInspectionRequestQuery): MutableList<SeasonalInspectionRequest> {
    return query.apply(store.list())
}
