package com.acme.transport.freightpay.ratecard.digest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional rate card notices by code. */
interface RegionalRateCardNoticeStore {
    fun find(code: String): RegionalRateCardNotice?
    fun save(item: RegionalRateCardNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalRateCardNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalRateCardNoticeStore(
    var items: MutableMap<String, RegionalRateCardNotice>,
    var order: MutableList<String>,
) : RegionalRateCardNoticeStore {
    override fun find(code: String): RegionalRateCardNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalRateCardNotice) {
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

    override fun list(): MutableList<RegionalRateCardNotice> {
        val out = mutableListOf<RegionalRateCardNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalRateCardNoticeStore(): RegionalRateCardNoticeStore {
    return MemoryRegionalRateCardNoticeStore(
        mutableMapOf<String, RegionalRateCardNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalRateCardNoticeStore(
    var inner: RegionalRateCardNoticeStore,
    var cache: TtlCache<RegionalRateCardNotice>,
    var hits: Int,
    var misses: Int,
) : RegionalRateCardNoticeStore {
    override fun find(code: String): RegionalRateCardNotice? {
        val hit = (cache.get(code) as RegionalRateCardNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalRateCardNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalRateCardNotice> = inner.list()
}

fun newCachedRegionalRateCardNoticeStore(inner: RegionalRateCardNoticeStore, ttl: Int, clock: Clock): RegionalRateCardNoticeStore {
    return CachedRegionalRateCardNoticeStore(
        inner = inner,
        cache = newTtlCache<RegionalRateCardNotice>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional rate card notices. Empty fields match all. */
data class RegionalRateCardNoticeQuery(
    var name: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalRateCardNotice): Boolean {
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

    fun apply(items: MutableList<RegionalRateCardNotice>): MutableList<RegionalRateCardNotice> {
        val matched = filter(items) { candidate: RegionalRateCardNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalRateCardNotices(store: RegionalRateCardNoticeStore, query: RegionalRateCardNoticeQuery): MutableList<RegionalRateCardNotice> {
    return query.apply(store.list())
}
