package com.acme.transport.lastmile.redelivery.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery notices by code. */
interface RedeliveryNoticeStore {
    fun find(code: String): RedeliveryNotice?
    fun save(item: RedeliveryNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryNoticeStore(
    var items: MutableMap<String, RedeliveryNotice>,
    var order: MutableList<String>,
) : RedeliveryNoticeStore {
    override fun find(code: String): RedeliveryNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryNotice) {
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

    override fun list(): MutableList<RedeliveryNotice> {
        val out = mutableListOf<RedeliveryNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryNoticeStore(): RedeliveryNoticeStore {
    return MemoryRedeliveryNoticeStore(mutableMapOf<String, RedeliveryNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryNoticeStore(
    var inner: RedeliveryNoticeStore,
    var cache: TtlCache<RedeliveryNotice>,
    var hits: Int,
    var misses: Int,
) : RedeliveryNoticeStore {
    override fun find(code: String): RedeliveryNotice? {
        val hit = (cache.get(code) as RedeliveryNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryNotice> = inner.list()
}

fun newCachedRedeliveryNoticeStore(inner: RedeliveryNoticeStore, ttl: Int, clock: Clock): RedeliveryNoticeStore {
    return CachedRedeliveryNoticeStore(
        inner = inner,
        cache = newTtlCache<RedeliveryNotice>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery notices. Empty fields match all. */
data class RedeliveryNoticeQuery(
    var country: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryNotice): Boolean {
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

    fun apply(items: MutableList<RedeliveryNotice>): MutableList<RedeliveryNotice> {
        val matched = filter(items) { candidate: RedeliveryNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryNotices(store: RedeliveryNoticeStore, query: RedeliveryNoticeQuery): MutableList<RedeliveryNotice> {
    return query.apply(store.list())
}
