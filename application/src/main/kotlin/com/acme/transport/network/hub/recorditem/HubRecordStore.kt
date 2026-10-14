package com.acme.transport.network.hub.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub records by code. */
interface HubRecordStore {
    fun find(code: String): HubRecord?
    fun save(item: HubRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubRecordStore(
    var items: MutableMap<String, HubRecord>,
    var order: MutableList<String>,
) : HubRecordStore {
    override fun find(code: String): HubRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubRecord) {
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

    override fun list(): MutableList<HubRecord> {
        val out = mutableListOf<HubRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubRecordStore(): HubRecordStore {
    return MemoryHubRecordStore(mutableMapOf<String, HubRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubRecordStore(
    var inner: HubRecordStore,
    var cache: TtlCache<HubRecord>,
    var hits: Int,
    var misses: Int,
) : HubRecordStore {
    override fun find(code: String): HubRecord? {
        val hit = (cache.get(code) as HubRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubRecord> = inner.list()
}

fun newCachedHubRecordStore(inner: HubRecordStore, ttl: Int, clock: Clock): HubRecordStore {
    return CachedHubRecordStore(
        inner = inner,
        cache = newTtlCache<HubRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub records. Empty fields match all. */
data class HubRecordQuery(
    var channel: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubRecord): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubRecord>): MutableList<HubRecord> {
        val matched = filter(items) { candidate: HubRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubRecords(store: HubRecordStore, query: HubRecordQuery): MutableList<HubRecord> {
    return query.apply(store.list())
}
