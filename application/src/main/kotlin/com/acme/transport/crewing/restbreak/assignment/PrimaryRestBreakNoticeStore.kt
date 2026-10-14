package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary rest break notices by code. */
interface PrimaryRestBreakNoticeStore {
    fun find(code: String): PrimaryRestBreakNotice?
    fun save(item: PrimaryRestBreakNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryRestBreakNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryRestBreakNoticeStore(
    var items: MutableMap<String, PrimaryRestBreakNotice>,
    var order: MutableList<String>,
) : PrimaryRestBreakNoticeStore {
    override fun find(code: String): PrimaryRestBreakNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryRestBreakNotice) {
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

    override fun list(): MutableList<PrimaryRestBreakNotice> {
        val out = mutableListOf<PrimaryRestBreakNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryRestBreakNoticeStore(): PrimaryRestBreakNoticeStore {
    return MemoryPrimaryRestBreakNoticeStore(
        mutableMapOf<String, PrimaryRestBreakNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryRestBreakNoticeStore(
    var inner: PrimaryRestBreakNoticeStore,
    var cache: TtlCache<PrimaryRestBreakNotice>,
    var hits: Int,
    var misses: Int,
) : PrimaryRestBreakNoticeStore {
    override fun find(code: String): PrimaryRestBreakNotice? {
        val hit = (cache.get(code) as PrimaryRestBreakNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryRestBreakNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryRestBreakNotice> = inner.list()
}

fun newCachedPrimaryRestBreakNoticeStore(inner: PrimaryRestBreakNoticeStore, ttl: Int, clock: Clock): PrimaryRestBreakNoticeStore {
    return CachedPrimaryRestBreakNoticeStore(
        inner = inner,
        cache = newTtlCache<PrimaryRestBreakNotice>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary rest break notices. Empty fields match all. */
data class PrimaryRestBreakNoticeQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryRestBreakNotice): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<PrimaryRestBreakNotice>): MutableList<PrimaryRestBreakNotice> {
        val matched = filter(items) { candidate: PrimaryRestBreakNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryRestBreakNotices(store: PrimaryRestBreakNoticeStore, query: PrimaryRestBreakNoticeQuery): MutableList<PrimaryRestBreakNotice> {
    return query.apply(store.list())
}
