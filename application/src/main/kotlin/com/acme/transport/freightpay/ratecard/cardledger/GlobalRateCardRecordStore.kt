package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global rate card records by code. */
interface GlobalRateCardRecordStore {
    fun find(code: String): GlobalRateCardRecord?
    fun save(item: GlobalRateCardRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalRateCardRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalRateCardRecordStore(
    var items: MutableMap<String, GlobalRateCardRecord>,
    var order: MutableList<String>,
) : GlobalRateCardRecordStore {
    override fun find(code: String): GlobalRateCardRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalRateCardRecord) {
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

    override fun list(): MutableList<GlobalRateCardRecord> {
        val out = mutableListOf<GlobalRateCardRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalRateCardRecordStore(): GlobalRateCardRecordStore {
    return MemoryGlobalRateCardRecordStore(
        mutableMapOf<String, GlobalRateCardRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalRateCardRecordStore(
    var inner: GlobalRateCardRecordStore,
    var cache: TtlCache<GlobalRateCardRecord>,
    var hits: Int,
    var misses: Int,
) : GlobalRateCardRecordStore {
    override fun find(code: String): GlobalRateCardRecord? {
        val hit = (cache.get(code) as GlobalRateCardRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalRateCardRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalRateCardRecord> = inner.list()
}

fun newCachedGlobalRateCardRecordStore(inner: GlobalRateCardRecordStore, ttl: Int, clock: Clock): GlobalRateCardRecordStore {
    return CachedGlobalRateCardRecordStore(
        inner = inner,
        cache = newTtlCache<GlobalRateCardRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global rate card records. Empty fields match all. */
data class GlobalRateCardRecordQuery(
    var label: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalRateCardRecord): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalRateCardRecord>): MutableList<GlobalRateCardRecord> {
        val matched = filter(items) { candidate: GlobalRateCardRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalRateCardRecords(store: GlobalRateCardRecordStore, query: GlobalRateCardRecordQuery): MutableList<GlobalRateCardRecord> {
    return query.apply(store.list())
}
