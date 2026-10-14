package com.acme.transport.customs.broker.reservation

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker records by code. */
interface BrokerRecordStore {
    fun find(code: String): BrokerRecord?
    fun save(item: BrokerRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerRecordStore(
    var items: MutableMap<String, BrokerRecord>,
    var order: MutableList<String>,
) : BrokerRecordStore {
    override fun find(code: String): BrokerRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerRecord) {
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

    override fun list(): MutableList<BrokerRecord> {
        val out = mutableListOf<BrokerRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerRecordStore(): BrokerRecordStore {
    return MemoryBrokerRecordStore(mutableMapOf<String, BrokerRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerRecordStore(
    var inner: BrokerRecordStore,
    var cache: TtlCache<BrokerRecord>,
    var hits: Int,
    var misses: Int,
) : BrokerRecordStore {
    override fun find(code: String): BrokerRecord? {
        val hit = (cache.get(code) as BrokerRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerRecord> = inner.list()
}

fun newCachedBrokerRecordStore(inner: BrokerRecordStore, ttl: Int, clock: Clock): BrokerRecordStore {
    return CachedBrokerRecordStore(
        inner = inner,
        cache = newTtlCache<BrokerRecord>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker records. Empty fields match all. */
data class BrokerRecordQuery(
    var segment: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerRecord): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerRecord>): MutableList<BrokerRecord> {
        val matched = filter(items) { candidate: BrokerRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerRecords(store: BrokerRecordStore, query: BrokerRecordQuery): MutableList<BrokerRecord> {
    return query.apply(store.list())
}
