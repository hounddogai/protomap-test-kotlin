package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps tiered drop notices by code. */
interface TieredDropNoticeStore {
    fun find(code: String): TieredDropNotice?
    fun save(item: TieredDropNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDropNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDropNoticeStore(
    var items: MutableMap<String, TieredDropNotice>,
    var order: MutableList<String>,
) : TieredDropNoticeStore {
    override fun find(code: String): TieredDropNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDropNotice) {
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

    override fun list(): MutableList<TieredDropNotice> {
        val out = mutableListOf<TieredDropNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDropNoticeStore(): TieredDropNoticeStore {
    return MemoryTieredDropNoticeStore(mutableMapOf<String, TieredDropNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDropNoticeStore(
    var inner: TieredDropNoticeStore,
    var cache: TtlCache<TieredDropNotice>,
    var hits: Int,
    var misses: Int,
) : TieredDropNoticeStore {
    override fun find(code: String): TieredDropNotice? {
        val hit = (cache.get(code) as TieredDropNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDropNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDropNotice> = inner.list()
}

fun newCachedTieredDropNoticeStore(inner: TieredDropNoticeStore, ttl: Int, clock: Clock): TieredDropNoticeStore {
    return CachedTieredDropNoticeStore(
        inner = inner,
        cache = newTtlCache<TieredDropNotice>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered drop notices. Empty fields match all. */
data class TieredDropNoticeQuery(
    var locale: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDropNotice): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<TieredDropNotice>): MutableList<TieredDropNotice> {
        val matched = filter(items) { candidate: TieredDropNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDropNotices(store: TieredDropNoticeStore, query: TieredDropNoticeQuery): MutableList<TieredDropNotice> {
    return query.apply(store.list())
}
