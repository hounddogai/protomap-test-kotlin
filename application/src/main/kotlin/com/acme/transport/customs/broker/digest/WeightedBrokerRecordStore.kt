package com.acme.transport.customs.broker.digest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted broker records by code. */
interface WeightedBrokerRecordStore {
    fun find(code: String): WeightedBrokerRecord?
    fun save(item: WeightedBrokerRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedBrokerRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedBrokerRecordStore(
    var items: MutableMap<String, WeightedBrokerRecord>,
    var order: MutableList<String>,
) : WeightedBrokerRecordStore {
    override fun find(code: String): WeightedBrokerRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedBrokerRecord) {
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

    override fun list(): MutableList<WeightedBrokerRecord> {
        val out = mutableListOf<WeightedBrokerRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedBrokerRecordStore(): WeightedBrokerRecordStore {
    return MemoryWeightedBrokerRecordStore(
        mutableMapOf<String, WeightedBrokerRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedBrokerRecordStore(
    var inner: WeightedBrokerRecordStore,
    var cache: TtlCache<WeightedBrokerRecord>,
    var hits: Int,
    var misses: Int,
) : WeightedBrokerRecordStore {
    override fun find(code: String): WeightedBrokerRecord? {
        val hit = (cache.get(code) as WeightedBrokerRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedBrokerRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedBrokerRecord> = inner.list()
}

fun newCachedWeightedBrokerRecordStore(inner: WeightedBrokerRecordStore, ttl: Int, clock: Clock): WeightedBrokerRecordStore {
    return CachedWeightedBrokerRecordStore(
        inner = inner,
        cache = newTtlCache<WeightedBrokerRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted broker records. Empty fields match all. */
data class WeightedBrokerRecordQuery(
    var category: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedBrokerRecord): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedBrokerRecord>): MutableList<WeightedBrokerRecord> {
        val matched = filter(items) { candidate: WeightedBrokerRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedBrokerRecords(store: WeightedBrokerRecordStore, query: WeightedBrokerRecordQuery): MutableList<WeightedBrokerRecord> {
    return query.apply(store.list())
}
