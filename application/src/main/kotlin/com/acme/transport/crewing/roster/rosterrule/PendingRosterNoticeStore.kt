package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending roster notices by code. */
interface PendingRosterNoticeStore {
    fun find(code: String): PendingRosterNotice?
    fun save(item: PendingRosterNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingRosterNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingRosterNoticeStore(
    var items: MutableMap<String, PendingRosterNotice>,
    var order: MutableList<String>,
) : PendingRosterNoticeStore {
    override fun find(code: String): PendingRosterNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingRosterNotice) {
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

    override fun list(): MutableList<PendingRosterNotice> {
        val out = mutableListOf<PendingRosterNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingRosterNoticeStore(): PendingRosterNoticeStore {
    return MemoryPendingRosterNoticeStore(mutableMapOf<String, PendingRosterNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingRosterNoticeStore(
    var inner: PendingRosterNoticeStore,
    var cache: TtlCache<PendingRosterNotice>,
    var hits: Int,
    var misses: Int,
) : PendingRosterNoticeStore {
    override fun find(code: String): PendingRosterNotice? {
        val hit = (cache.get(code) as PendingRosterNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingRosterNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingRosterNotice> = inner.list()
}

fun newCachedPendingRosterNoticeStore(inner: PendingRosterNoticeStore, ttl: Int, clock: Clock): PendingRosterNoticeStore {
    return CachedPendingRosterNoticeStore(
        inner = inner,
        cache = newTtlCache<PendingRosterNotice>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending roster notices. Empty fields match all. */
data class PendingRosterNoticeQuery(
    var channel: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingRosterNotice): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<PendingRosterNotice>): MutableList<PendingRosterNotice> {
        val matched = filter(items) { candidate: PendingRosterNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingRosterNotices(store: PendingRosterNoticeStore, query: PendingRosterNoticeQuery): MutableList<PendingRosterNotice> {
    return query.apply(store.list())
}
