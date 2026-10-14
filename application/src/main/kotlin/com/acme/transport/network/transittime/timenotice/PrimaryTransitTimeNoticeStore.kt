package com.acme.transport.network.transittime.timenotice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps primary transit time notices by code. */
interface PrimaryTransitTimeNoticeStore {
    fun find(code: String): PrimaryTransitTimeNotice?
    fun save(item: PrimaryTransitTimeNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTransitTimeNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTransitTimeNoticeStore(
    var items: MutableMap<String, PrimaryTransitTimeNotice>,
    var order: MutableList<String>,
) : PrimaryTransitTimeNoticeStore {
    override fun find(code: String): PrimaryTransitTimeNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTransitTimeNotice) {
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

    override fun list(): MutableList<PrimaryTransitTimeNotice> {
        val out = mutableListOf<PrimaryTransitTimeNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTransitTimeNoticeStore(): PrimaryTransitTimeNoticeStore {
    return MemoryPrimaryTransitTimeNoticeStore(
        mutableMapOf<String, PrimaryTransitTimeNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTransitTimeNoticeStore(
    var inner: PrimaryTransitTimeNoticeStore,
    var cache: TtlCache<PrimaryTransitTimeNotice>,
    var hits: Int,
    var misses: Int,
) : PrimaryTransitTimeNoticeStore {
    override fun find(code: String): PrimaryTransitTimeNotice? {
        val hit = (cache.get(code) as PrimaryTransitTimeNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTransitTimeNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTransitTimeNotice> = inner.list()
}

fun newCachedPrimaryTransitTimeNoticeStore(inner: PrimaryTransitTimeNoticeStore, ttl: Int, clock: Clock): PrimaryTransitTimeNoticeStore {
    return CachedPrimaryTransitTimeNoticeStore(
        inner = inner,
        cache = newTtlCache<PrimaryTransitTimeNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary transit time notices. Empty fields match all. */
data class PrimaryTransitTimeNoticeQuery(
    var category: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTransitTimeNotice): Boolean {
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

    fun apply(items: MutableList<PrimaryTransitTimeNotice>): MutableList<PrimaryTransitTimeNotice> {
        val matched = filter(items) { candidate: PrimaryTransitTimeNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTransitTimeNotices(store: PrimaryTransitTimeNoticeStore, query: PrimaryTransitTimeNoticeQuery): MutableList<PrimaryTransitTimeNotice> {
    return query.apply(store.list())
}
