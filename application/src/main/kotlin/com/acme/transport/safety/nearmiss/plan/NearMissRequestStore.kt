package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss requests by code. */
interface NearMissRequestStore {
    fun find(code: String): NearMissRequest?
    fun save(item: NearMissRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissRequestStore(
    var items: MutableMap<String, NearMissRequest>,
    var order: MutableList<String>,
) : NearMissRequestStore {
    override fun find(code: String): NearMissRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissRequest) {
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

    override fun list(): MutableList<NearMissRequest> {
        val out = mutableListOf<NearMissRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissRequestStore(): NearMissRequestStore {
    return MemoryNearMissRequestStore(mutableMapOf<String, NearMissRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissRequestStore(
    var inner: NearMissRequestStore,
    var cache: TtlCache<NearMissRequest>,
    var hits: Int,
    var misses: Int,
) : NearMissRequestStore {
    override fun find(code: String): NearMissRequest? {
        val hit = (cache.get(code) as NearMissRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissRequest> = inner.list()
}

fun newCachedNearMissRequestStore(inner: NearMissRequestStore, ttl: Int, clock: Clock): NearMissRequestStore {
    return CachedNearMissRequestStore(
        inner = inner,
        cache = newTtlCache<NearMissRequest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss requests. Empty fields match all. */
data class NearMissRequestQuery(
    var lane: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissRequest): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissRequest>): MutableList<NearMissRequest> {
        val matched = filter(items) { candidate: NearMissRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissRequests(store: NearMissRequestStore, query: NearMissRequestQuery): MutableList<NearMissRequest> {
    return query.apply(store.list())
}
