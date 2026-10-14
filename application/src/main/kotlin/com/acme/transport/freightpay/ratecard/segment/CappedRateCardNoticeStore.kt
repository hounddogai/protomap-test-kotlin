package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped rate card notices by code. */
interface CappedRateCardNoticeStore {
    fun find(code: String): CappedRateCardNotice?
    fun save(item: CappedRateCardNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedRateCardNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedRateCardNoticeStore(
    var items: MutableMap<String, CappedRateCardNotice>,
    var order: MutableList<String>,
) : CappedRateCardNoticeStore {
    override fun find(code: String): CappedRateCardNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedRateCardNotice) {
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

    override fun list(): MutableList<CappedRateCardNotice> {
        val out = mutableListOf<CappedRateCardNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedRateCardNoticeStore(): CappedRateCardNoticeStore {
    return MemoryCappedRateCardNoticeStore(
        mutableMapOf<String, CappedRateCardNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedRateCardNoticeStore(
    var inner: CappedRateCardNoticeStore,
    var cache: TtlCache<CappedRateCardNotice>,
    var hits: Int,
    var misses: Int,
) : CappedRateCardNoticeStore {
    override fun find(code: String): CappedRateCardNotice? {
        val hit = (cache.get(code) as CappedRateCardNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedRateCardNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedRateCardNotice> = inner.list()
}

fun newCachedCappedRateCardNoticeStore(inner: CappedRateCardNoticeStore, ttl: Int, clock: Clock): CappedRateCardNoticeStore {
    return CachedCappedRateCardNoticeStore(
        inner = inner,
        cache = newTtlCache<CappedRateCardNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped rate card notices. Empty fields match all. */
data class CappedRateCardNoticeQuery(
    var title: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedRateCardNotice): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedRateCardNotice>): MutableList<CappedRateCardNotice> {
        val matched = filter(items) { candidate: CappedRateCardNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedRateCardNotices(store: CappedRateCardNoticeStore, query: CappedRateCardNoticeQuery): MutableList<CappedRateCardNotice> {
    return query.apply(store.list())
}
