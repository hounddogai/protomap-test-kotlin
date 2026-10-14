package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive service plan records by code. */
interface AdaptiveServicePlanRecordStore {
    fun find(code: String): AdaptiveServicePlanRecord?
    fun save(item: AdaptiveServicePlanRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveServicePlanRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveServicePlanRecordStore(
    var items: MutableMap<String, AdaptiveServicePlanRecord>,
    var order: MutableList<String>,
) : AdaptiveServicePlanRecordStore {
    override fun find(code: String): AdaptiveServicePlanRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveServicePlanRecord) {
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

    override fun list(): MutableList<AdaptiveServicePlanRecord> {
        val out = mutableListOf<AdaptiveServicePlanRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveServicePlanRecordStore(): AdaptiveServicePlanRecordStore {
    return MemoryAdaptiveServicePlanRecordStore(
        mutableMapOf<String, AdaptiveServicePlanRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveServicePlanRecordStore(
    var inner: AdaptiveServicePlanRecordStore,
    var cache: TtlCache<AdaptiveServicePlanRecord>,
    var hits: Int,
    var misses: Int,
) : AdaptiveServicePlanRecordStore {
    override fun find(code: String): AdaptiveServicePlanRecord? {
        val hit = (cache.get(code) as AdaptiveServicePlanRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveServicePlanRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveServicePlanRecord> = inner.list()
}

fun newCachedAdaptiveServicePlanRecordStore(inner: AdaptiveServicePlanRecordStore, ttl: Int, clock: Clock): AdaptiveServicePlanRecordStore {
    return CachedAdaptiveServicePlanRecordStore(
        inner = inner,
        cache = newTtlCache<AdaptiveServicePlanRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive service plan records. Empty fields match all. */
data class AdaptiveServicePlanRecordQuery(
    var channel: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveServicePlanRecord): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveServicePlanRecord>): MutableList<AdaptiveServicePlanRecord> {
        val matched = filter(items) { candidate: AdaptiveServicePlanRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveServicePlanRecords(store: AdaptiveServicePlanRecordStore, query: AdaptiveServicePlanRecordQuery): MutableList<AdaptiveServicePlanRecord> {
    return query.apply(store.list())
}
