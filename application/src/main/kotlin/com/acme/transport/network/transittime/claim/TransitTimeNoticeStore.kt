package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time notices by code. */
interface TransitTimeNoticeStore {
    fun find(code: String): TransitTimeNotice?
    fun save(item: TransitTimeNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeNoticeStore(
    var items: MutableMap<String, TransitTimeNotice>,
    var order: MutableList<String>,
) : TransitTimeNoticeStore {
    override fun find(code: String): TransitTimeNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeNotice) {
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

    override fun list(): MutableList<TransitTimeNotice> {
        val out = mutableListOf<TransitTimeNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeNoticeStore(): TransitTimeNoticeStore {
    return MemoryTransitTimeNoticeStore(mutableMapOf<String, TransitTimeNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeNoticeStore(
    var inner: TransitTimeNoticeStore,
    var cache: TtlCache<TransitTimeNotice>,
    var hits: Int,
    var misses: Int,
) : TransitTimeNoticeStore {
    override fun find(code: String): TransitTimeNotice? {
        val hit = (cache.get(code) as TransitTimeNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeNotice> = inner.list()
}

fun newCachedTransitTimeNoticeStore(inner: TransitTimeNoticeStore, ttl: Int, clock: Clock): TransitTimeNoticeStore {
    return CachedTransitTimeNoticeStore(
        inner = inner,
        cache = newTtlCache<TransitTimeNotice>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time notices. Empty fields match all. */
data class TransitTimeNoticeQuery(
    var title: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeNotice): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<TransitTimeNotice>): MutableList<TransitTimeNotice> {
        val matched = filter(items) { candidate: TransitTimeNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeNotices(store: TransitTimeNoticeStore, query: TransitTimeNoticeQuery): MutableList<TransitTimeNotice> {
    return query.apply(store.list())
}
