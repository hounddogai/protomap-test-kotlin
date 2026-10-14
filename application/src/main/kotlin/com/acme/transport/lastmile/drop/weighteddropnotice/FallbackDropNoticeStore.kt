package com.acme.transport.lastmile.drop.weighteddropnotice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps fallback drop notices by code. */
interface FallbackDropNoticeStore {
    fun find(code: String): FallbackDropNotice?
    fun save(item: FallbackDropNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDropNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDropNoticeStore(
    var items: MutableMap<String, FallbackDropNotice>,
    var order: MutableList<String>,
) : FallbackDropNoticeStore {
    override fun find(code: String): FallbackDropNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDropNotice) {
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

    override fun list(): MutableList<FallbackDropNotice> {
        val out = mutableListOf<FallbackDropNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDropNoticeStore(): FallbackDropNoticeStore {
    return MemoryFallbackDropNoticeStore(mutableMapOf<String, FallbackDropNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDropNoticeStore(
    var inner: FallbackDropNoticeStore,
    var cache: TtlCache<FallbackDropNotice>,
    var hits: Int,
    var misses: Int,
) : FallbackDropNoticeStore {
    override fun find(code: String): FallbackDropNotice? {
        val hit = (cache.get(code) as FallbackDropNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDropNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDropNotice> = inner.list()
}

fun newCachedFallbackDropNoticeStore(inner: FallbackDropNoticeStore, ttl: Int, clock: Clock): FallbackDropNoticeStore {
    return CachedFallbackDropNoticeStore(
        inner = inner,
        cache = newTtlCache<FallbackDropNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback drop notices. Empty fields match all. */
data class FallbackDropNoticeQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDropNotice): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDropNotice>): MutableList<FallbackDropNotice> {
        val matched = filter(items) { candidate: FallbackDropNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDropNotices(store: FallbackDropNoticeStore, query: FallbackDropNoticeQuery): MutableList<FallbackDropNotice> {
    return query.apply(store.list())
}
