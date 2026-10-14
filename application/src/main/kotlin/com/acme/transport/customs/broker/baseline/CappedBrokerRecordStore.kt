package com.acme.transport.customs.broker.baseline

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped broker records by code. */
interface CappedBrokerRecordStore {
    fun find(code: String): CappedBrokerRecord?
    fun save(item: CappedBrokerRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedBrokerRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedBrokerRecordStore(
    var items: MutableMap<String, CappedBrokerRecord>,
    var order: MutableList<String>,
) : CappedBrokerRecordStore {
    override fun find(code: String): CappedBrokerRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedBrokerRecord) {
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

    override fun list(): MutableList<CappedBrokerRecord> {
        val out = mutableListOf<CappedBrokerRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedBrokerRecordStore(): CappedBrokerRecordStore {
    return MemoryCappedBrokerRecordStore(mutableMapOf<String, CappedBrokerRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedBrokerRecordStore(
    var inner: CappedBrokerRecordStore,
    var cache: TtlCache<CappedBrokerRecord>,
    var hits: Int,
    var misses: Int,
) : CappedBrokerRecordStore {
    override fun find(code: String): CappedBrokerRecord? {
        val hit = (cache.get(code) as CappedBrokerRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedBrokerRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedBrokerRecord> = inner.list()
}

fun newCachedCappedBrokerRecordStore(inner: CappedBrokerRecordStore, ttl: Int, clock: Clock): CappedBrokerRecordStore {
    return CachedCappedBrokerRecordStore(
        inner = inner,
        cache = newTtlCache<CappedBrokerRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped broker records. Empty fields match all. */
data class CappedBrokerRecordQuery(
    var locale: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedBrokerRecord): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<CappedBrokerRecord>): MutableList<CappedBrokerRecord> {
        val matched = filter(items) { candidate: CappedBrokerRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedBrokerRecords(store: CappedBrokerRecordStore, query: CappedBrokerRecordQuery): MutableList<CappedBrokerRecord> {
    return query.apply(store.list())
}
