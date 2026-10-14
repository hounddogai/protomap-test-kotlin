package com.acme.transport.lastmile.parcel.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel requests by code. */
interface ParcelRequestStore {
    fun find(code: String): ParcelRequest?
    fun save(item: ParcelRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelRequestStore(
    var items: MutableMap<String, ParcelRequest>,
    var order: MutableList<String>,
) : ParcelRequestStore {
    override fun find(code: String): ParcelRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelRequest) {
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

    override fun list(): MutableList<ParcelRequest> {
        val out = mutableListOf<ParcelRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelRequestStore(): ParcelRequestStore {
    return MemoryParcelRequestStore(mutableMapOf<String, ParcelRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelRequestStore(
    var inner: ParcelRequestStore,
    var cache: TtlCache<ParcelRequest>,
    var hits: Int,
    var misses: Int,
) : ParcelRequestStore {
    override fun find(code: String): ParcelRequest? {
        val hit = (cache.get(code) as ParcelRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelRequest> = inner.list()
}

fun newCachedParcelRequestStore(inner: ParcelRequestStore, ttl: Int, clock: Clock): ParcelRequestStore {
    return CachedParcelRequestStore(
        inner = inner,
        cache = newTtlCache<ParcelRequest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel requests. Empty fields match all. */
data class ParcelRequestQuery(
    var label: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelRequest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelRequest>): MutableList<ParcelRequest> {
        val matched = filter(items) { candidate: ParcelRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelRequests(store: ParcelRequestStore, query: ParcelRequestQuery): MutableList<ParcelRequest> {
    return query.apply(store.list())
}
