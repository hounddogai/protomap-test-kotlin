package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset records by code. */
interface RouteOffsetRecordStore {
    fun find(code: String): RouteOffsetRecord?
    fun save(item: RouteOffsetRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetRecordStore(
    var items: MutableMap<String, RouteOffsetRecord>,
    var order: MutableList<String>,
) : RouteOffsetRecordStore {
    override fun find(code: String): RouteOffsetRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetRecord) {
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

    override fun list(): MutableList<RouteOffsetRecord> {
        val out = mutableListOf<RouteOffsetRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetRecordStore(): RouteOffsetRecordStore {
    return MemoryRouteOffsetRecordStore(mutableMapOf<String, RouteOffsetRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetRecordStore(
    var inner: RouteOffsetRecordStore,
    var cache: TtlCache<RouteOffsetRecord>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetRecordStore {
    override fun find(code: String): RouteOffsetRecord? {
        val hit = (cache.get(code) as RouteOffsetRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetRecord> = inner.list()
}

fun newCachedRouteOffsetRecordStore(inner: RouteOffsetRecordStore, ttl: Int, clock: Clock): RouteOffsetRecordStore {
    return CachedRouteOffsetRecordStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset records. Empty fields match all. */
data class RouteOffsetRecordQuery(
    var name: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetRecord): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetRecord>): MutableList<RouteOffsetRecord> {
        val matched = filter(items) { candidate: RouteOffsetRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetRecords(store: RouteOffsetRecordStore, query: RouteOffsetRecordQuery): MutableList<RouteOffsetRecord> {
    return query.apply(store.list())
}
