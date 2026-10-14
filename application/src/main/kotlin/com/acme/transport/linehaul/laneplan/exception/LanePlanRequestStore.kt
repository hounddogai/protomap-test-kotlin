package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan requests by code. */
interface LanePlanRequestStore {
    fun find(code: String): LanePlanRequest?
    fun save(item: LanePlanRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanRequestStore(
    var items: MutableMap<String, LanePlanRequest>,
    var order: MutableList<String>,
) : LanePlanRequestStore {
    override fun find(code: String): LanePlanRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanRequest) {
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

    override fun list(): MutableList<LanePlanRequest> {
        val out = mutableListOf<LanePlanRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanRequestStore(): LanePlanRequestStore {
    return MemoryLanePlanRequestStore(mutableMapOf<String, LanePlanRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanRequestStore(
    var inner: LanePlanRequestStore,
    var cache: TtlCache<LanePlanRequest>,
    var hits: Int,
    var misses: Int,
) : LanePlanRequestStore {
    override fun find(code: String): LanePlanRequest? {
        val hit = (cache.get(code) as LanePlanRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanRequest> = inner.list()
}

fun newCachedLanePlanRequestStore(inner: LanePlanRequestStore, ttl: Int, clock: Clock): LanePlanRequestStore {
    return CachedLanePlanRequestStore(
        inner = inner,
        cache = newTtlCache<LanePlanRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan requests. Empty fields match all. */
data class LanePlanRequestQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanRequest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<LanePlanRequest>): MutableList<LanePlanRequest> {
        val matched = filter(items) { candidate: LanePlanRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanRequests(store: LanePlanRequestStore, query: LanePlanRequestQuery): MutableList<LanePlanRequest> {
    return query.apply(store.list())
}
