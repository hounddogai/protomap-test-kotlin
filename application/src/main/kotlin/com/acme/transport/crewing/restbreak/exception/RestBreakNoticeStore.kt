package com.acme.transport.crewing.restbreak.exception

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break notices by code. */
interface RestBreakNoticeStore {
    fun find(code: String): RestBreakNotice?
    fun save(item: RestBreakNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakNoticeStore(
    var items: MutableMap<String, RestBreakNotice>,
    var order: MutableList<String>,
) : RestBreakNoticeStore {
    override fun find(code: String): RestBreakNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakNotice) {
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

    override fun list(): MutableList<RestBreakNotice> {
        val out = mutableListOf<RestBreakNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakNoticeStore(): RestBreakNoticeStore {
    return MemoryRestBreakNoticeStore(mutableMapOf<String, RestBreakNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakNoticeStore(
    var inner: RestBreakNoticeStore,
    var cache: TtlCache<RestBreakNotice>,
    var hits: Int,
    var misses: Int,
) : RestBreakNoticeStore {
    override fun find(code: String): RestBreakNotice? {
        val hit = (cache.get(code) as RestBreakNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakNotice> = inner.list()
}

fun newCachedRestBreakNoticeStore(inner: RestBreakNoticeStore, ttl: Int, clock: Clock): RestBreakNoticeStore {
    return CachedRestBreakNoticeStore(
        inner = inner,
        cache = newTtlCache<RestBreakNotice>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break notices. Empty fields match all. */
data class RestBreakNoticeQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakNotice): Boolean {
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

    fun apply(items: MutableList<RestBreakNotice>): MutableList<RestBreakNotice> {
        val matched = filter(items) { candidate: RestBreakNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakNotices(store: RestBreakNoticeStore, query: RestBreakNoticeQuery): MutableList<RestBreakNotice> {
    return query.apply(store.list())
}
