package com.acme.transport.network.transittime.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps strict transit time notices by code. */
interface StrictTransitTimeNoticeStore {
    fun find(code: String): StrictTransitTimeNotice?
    fun save(item: StrictTransitTimeNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictTransitTimeNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictTransitTimeNoticeStore(
    var items: MutableMap<String, StrictTransitTimeNotice>,
    var order: MutableList<String>,
) : StrictTransitTimeNoticeStore {
    override fun find(code: String): StrictTransitTimeNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictTransitTimeNotice) {
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

    override fun list(): MutableList<StrictTransitTimeNotice> {
        val out = mutableListOf<StrictTransitTimeNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictTransitTimeNoticeStore(): StrictTransitTimeNoticeStore {
    return MemoryStrictTransitTimeNoticeStore(
        mutableMapOf<String, StrictTransitTimeNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictTransitTimeNoticeStore(
    var inner: StrictTransitTimeNoticeStore,
    var cache: TtlCache<StrictTransitTimeNotice>,
    var hits: Int,
    var misses: Int,
) : StrictTransitTimeNoticeStore {
    override fun find(code: String): StrictTransitTimeNotice? {
        val hit = (cache.get(code) as StrictTransitTimeNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictTransitTimeNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictTransitTimeNotice> = inner.list()
}

fun newCachedStrictTransitTimeNoticeStore(inner: StrictTransitTimeNoticeStore, ttl: Int, clock: Clock): StrictTransitTimeNoticeStore {
    return CachedStrictTransitTimeNoticeStore(
        inner = inner,
        cache = newTtlCache<StrictTransitTimeNotice>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict transit time notices. Empty fields match all. */
data class StrictTransitTimeNoticeQuery(
    var country: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictTransitTimeNotice): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictTransitTimeNotice>): MutableList<StrictTransitTimeNotice> {
        val matched = filter(items) { candidate: StrictTransitTimeNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictTransitTimeNotices(store: StrictTransitTimeNoticeStore, query: StrictTransitTimeNoticeQuery): MutableList<StrictTransitTimeNotice> {
    return query.apply(store.list())
}
