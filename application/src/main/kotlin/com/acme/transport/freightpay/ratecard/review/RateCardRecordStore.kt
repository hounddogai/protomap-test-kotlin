package com.acme.transport.freightpay.ratecard.review

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card records by code. */
interface RateCardRecordStore {
    fun find(code: String): RateCardRecord?
    fun save(item: RateCardRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardRecordStore(
    var items: MutableMap<String, RateCardRecord>,
    var order: MutableList<String>,
) : RateCardRecordStore {
    override fun find(code: String): RateCardRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardRecord) {
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

    override fun list(): MutableList<RateCardRecord> {
        val out = mutableListOf<RateCardRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardRecordStore(): RateCardRecordStore {
    return MemoryRateCardRecordStore(mutableMapOf<String, RateCardRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardRecordStore(
    var inner: RateCardRecordStore,
    var cache: TtlCache<RateCardRecord>,
    var hits: Int,
    var misses: Int,
) : RateCardRecordStore {
    override fun find(code: String): RateCardRecord? {
        val hit = (cache.get(code) as RateCardRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardRecord> = inner.list()
}

fun newCachedRateCardRecordStore(inner: RateCardRecordStore, ttl: Int, clock: Clock): RateCardRecordStore {
    return CachedRateCardRecordStore(
        inner = inner,
        cache = newTtlCache<RateCardRecord>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card records. Empty fields match all. */
data class RateCardRecordQuery(
    var region: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardRecord): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardRecord>): MutableList<RateCardRecord> {
        val matched = filter(items) { candidate: RateCardRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardRecords(store: RateCardRecordStore, query: RateCardRecordQuery): MutableList<RateCardRecord> {
    return query.apply(store.list())
}
