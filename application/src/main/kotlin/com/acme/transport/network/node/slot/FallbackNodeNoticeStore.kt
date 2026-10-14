package com.acme.transport.network.node.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps fallback node notices by code. */
interface FallbackNodeNoticeStore {
    fun find(code: String): FallbackNodeNotice?
    fun save(item: FallbackNodeNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackNodeNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackNodeNoticeStore(
    var items: MutableMap<String, FallbackNodeNotice>,
    var order: MutableList<String>,
) : FallbackNodeNoticeStore {
    override fun find(code: String): FallbackNodeNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackNodeNotice) {
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

    override fun list(): MutableList<FallbackNodeNotice> {
        val out = mutableListOf<FallbackNodeNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackNodeNoticeStore(): FallbackNodeNoticeStore {
    return MemoryFallbackNodeNoticeStore(mutableMapOf<String, FallbackNodeNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackNodeNoticeStore(
    var inner: FallbackNodeNoticeStore,
    var cache: TtlCache<FallbackNodeNotice>,
    var hits: Int,
    var misses: Int,
) : FallbackNodeNoticeStore {
    override fun find(code: String): FallbackNodeNotice? {
        val hit = (cache.get(code) as FallbackNodeNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackNodeNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackNodeNotice> = inner.list()
}

fun newCachedFallbackNodeNoticeStore(inner: FallbackNodeNoticeStore, ttl: Int, clock: Clock): FallbackNodeNoticeStore {
    return CachedFallbackNodeNoticeStore(
        inner = inner,
        cache = newTtlCache<FallbackNodeNotice>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback node notices. Empty fields match all. */
data class FallbackNodeNoticeQuery(
    var name: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackNodeNotice): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackNodeNotice>): MutableList<FallbackNodeNotice> {
        val matched = filter(items) { candidate: FallbackNodeNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackNodeNotices(store: FallbackNodeNoticeStore, query: FallbackNodeNoticeQuery): MutableList<FallbackNodeNotice> {
    return query.apply(store.list())
}
