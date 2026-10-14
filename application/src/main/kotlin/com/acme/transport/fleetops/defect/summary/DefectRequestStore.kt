package com.acme.transport.fleetops.defect.summary

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect requests by code. */
interface DefectRequestStore {
    fun find(code: String): DefectRequest?
    fun save(item: DefectRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectRequestStore(
    var items: MutableMap<String, DefectRequest>,
    var order: MutableList<String>,
) : DefectRequestStore {
    override fun find(code: String): DefectRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectRequest) {
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

    override fun list(): MutableList<DefectRequest> {
        val out = mutableListOf<DefectRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectRequestStore(): DefectRequestStore {
    return MemoryDefectRequestStore(mutableMapOf<String, DefectRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectRequestStore(
    var inner: DefectRequestStore,
    var cache: TtlCache<DefectRequest>,
    var hits: Int,
    var misses: Int,
) : DefectRequestStore {
    override fun find(code: String): DefectRequest? {
        val hit = (cache.get(code) as DefectRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectRequest> = inner.list()
}

fun newCachedDefectRequestStore(inner: DefectRequestStore, ttl: Int, clock: Clock): DefectRequestStore {
    return CachedDefectRequestStore(
        inner = inner,
        cache = newTtlCache<DefectRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect requests. Empty fields match all. */
data class DefectRequestQuery(
    var channel: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectRequest): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectRequest>): MutableList<DefectRequest> {
        val matched = filter(items) { candidate: DefectRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectRequests(store: DefectRequestStore, query: DefectRequestQuery): MutableList<DefectRequest> {
    return query.apply(store.list())
}
