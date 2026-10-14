package com.acme.transport.lastmile.drop.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop notices by code. */
interface DropNoticeStore {
    fun find(code: String): DropNotice?
    fun save(item: DropNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropNoticeStore(
    var items: MutableMap<String, DropNotice>,
    var order: MutableList<String>,
) : DropNoticeStore {
    override fun find(code: String): DropNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropNotice) {
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

    override fun list(): MutableList<DropNotice> {
        val out = mutableListOf<DropNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropNoticeStore(): DropNoticeStore {
    return MemoryDropNoticeStore(mutableMapOf<String, DropNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropNoticeStore(
    var inner: DropNoticeStore,
    var cache: TtlCache<DropNotice>,
    var hits: Int,
    var misses: Int,
) : DropNoticeStore {
    override fun find(code: String): DropNotice? {
        val hit = (cache.get(code) as DropNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropNotice> = inner.list()
}

fun newCachedDropNoticeStore(inner: DropNoticeStore, ttl: Int, clock: Clock): DropNoticeStore {
    return CachedDropNoticeStore(
        inner = inner,
        cache = newTtlCache<DropNotice>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop notices. Empty fields match all. */
data class DropNoticeQuery(
    var lane: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropNotice): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<DropNotice>): MutableList<DropNotice> {
        val matched = filter(items) { candidate: DropNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropNotices(store: DropNoticeStore, query: DropNoticeQuery): MutableList<DropNotice> {
    return query.apply(store.list())
}
