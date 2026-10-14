package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card notices by code. */
interface RateCardNoticeStore {
    fun find(code: String): RateCardNotice?
    fun save(item: RateCardNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardNoticeStore(
    var items: MutableMap<String, RateCardNotice>,
    var order: MutableList<String>,
) : RateCardNoticeStore {
    override fun find(code: String): RateCardNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardNotice) {
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

    override fun list(): MutableList<RateCardNotice> {
        val out = mutableListOf<RateCardNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardNoticeStore(): RateCardNoticeStore {
    return MemoryRateCardNoticeStore(mutableMapOf<String, RateCardNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardNoticeStore(
    var inner: RateCardNoticeStore,
    var cache: TtlCache<RateCardNotice>,
    var hits: Int,
    var misses: Int,
) : RateCardNoticeStore {
    override fun find(code: String): RateCardNotice? {
        val hit = (cache.get(code) as RateCardNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardNotice> = inner.list()
}

fun newCachedRateCardNoticeStore(inner: RateCardNoticeStore, ttl: Int, clock: Clock): RateCardNoticeStore {
    return CachedRateCardNoticeStore(
        inner = inner,
        cache = newTtlCache<RateCardNotice>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card notices. Empty fields match all. */
data class RateCardNoticeQuery(
    var channel: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardNotice): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardNotice>): MutableList<RateCardNotice> {
        val matched = filter(items) { candidate: RateCardNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardNotices(store: RateCardNoticeStore, query: RateCardNoticeQuery): MutableList<RateCardNotice> {
    return query.apply(store.list())
}
