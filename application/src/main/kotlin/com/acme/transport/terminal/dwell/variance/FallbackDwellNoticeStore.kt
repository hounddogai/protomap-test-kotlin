package com.acme.transport.terminal.dwell.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps fallback dwell notices by code. */
interface FallbackDwellNoticeStore {
    fun find(code: String): FallbackDwellNotice?
    fun save(item: FallbackDwellNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDwellNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDwellNoticeStore(
    var items: MutableMap<String, FallbackDwellNotice>,
    var order: MutableList<String>,
) : FallbackDwellNoticeStore {
    override fun find(code: String): FallbackDwellNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDwellNotice) {
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

    override fun list(): MutableList<FallbackDwellNotice> {
        val out = mutableListOf<FallbackDwellNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDwellNoticeStore(): FallbackDwellNoticeStore {
    return MemoryFallbackDwellNoticeStore(mutableMapOf<String, FallbackDwellNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDwellNoticeStore(
    var inner: FallbackDwellNoticeStore,
    var cache: TtlCache<FallbackDwellNotice>,
    var hits: Int,
    var misses: Int,
) : FallbackDwellNoticeStore {
    override fun find(code: String): FallbackDwellNotice? {
        val hit = (cache.get(code) as FallbackDwellNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDwellNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDwellNotice> = inner.list()
}

fun newCachedFallbackDwellNoticeStore(inner: FallbackDwellNoticeStore, ttl: Int, clock: Clock): FallbackDwellNoticeStore {
    return CachedFallbackDwellNoticeStore(
        inner = inner,
        cache = newTtlCache<FallbackDwellNotice>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback dwell notices. Empty fields match all. */
data class FallbackDwellNoticeQuery(
    var label: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDwellNotice): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDwellNotice>): MutableList<FallbackDwellNotice> {
        val matched = filter(items) { candidate: FallbackDwellNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDwellNotices(store: FallbackDwellNoticeStore, query: FallbackDwellNoticeQuery): MutableList<FallbackDwellNotice> {
    return query.apply(store.list())
}
