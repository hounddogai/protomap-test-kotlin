package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps capped transit time notices by code. */
interface CappedTransitTimeNoticeStore {
    fun find(code: String): CappedTransitTimeNotice?
    fun save(item: CappedTransitTimeNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedTransitTimeNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedTransitTimeNoticeStore(
    var items: MutableMap<String, CappedTransitTimeNotice>,
    var order: MutableList<String>,
) : CappedTransitTimeNoticeStore {
    override fun find(code: String): CappedTransitTimeNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedTransitTimeNotice) {
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

    override fun list(): MutableList<CappedTransitTimeNotice> {
        val out = mutableListOf<CappedTransitTimeNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedTransitTimeNoticeStore(): CappedTransitTimeNoticeStore {
    return MemoryCappedTransitTimeNoticeStore(
        mutableMapOf<String, CappedTransitTimeNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedTransitTimeNoticeStore(
    var inner: CappedTransitTimeNoticeStore,
    var cache: TtlCache<CappedTransitTimeNotice>,
    var hits: Int,
    var misses: Int,
) : CappedTransitTimeNoticeStore {
    override fun find(code: String): CappedTransitTimeNotice? {
        val hit = (cache.get(code) as CappedTransitTimeNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedTransitTimeNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedTransitTimeNotice> = inner.list()
}

fun newCachedCappedTransitTimeNoticeStore(inner: CappedTransitTimeNoticeStore, ttl: Int, clock: Clock): CappedTransitTimeNoticeStore {
    return CachedCappedTransitTimeNoticeStore(
        inner = inner,
        cache = newTtlCache<CappedTransitTimeNotice>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped transit time notices. Empty fields match all. */
data class CappedTransitTimeNoticeQuery(
    var source: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedTransitTimeNotice): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedTransitTimeNotice>): MutableList<CappedTransitTimeNotice> {
        val matched = filter(items) { candidate: CappedTransitTimeNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedTransitTimeNotices(store: CappedTransitTimeNoticeStore, query: CappedTransitTimeNoticeQuery): MutableList<CappedTransitTimeNotice> {
    return query.apply(store.list())
}
