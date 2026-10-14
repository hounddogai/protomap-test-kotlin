package com.acme.transport.crewing.roster.template

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary roster notices by code. */
interface PrimaryRosterNoticeStore {
    fun find(code: String): PrimaryRosterNotice?
    fun save(item: PrimaryRosterNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryRosterNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryRosterNoticeStore(
    var items: MutableMap<String, PrimaryRosterNotice>,
    var order: MutableList<String>,
) : PrimaryRosterNoticeStore {
    override fun find(code: String): PrimaryRosterNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryRosterNotice) {
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

    override fun list(): MutableList<PrimaryRosterNotice> {
        val out = mutableListOf<PrimaryRosterNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryRosterNoticeStore(): PrimaryRosterNoticeStore {
    return MemoryPrimaryRosterNoticeStore(mutableMapOf<String, PrimaryRosterNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryRosterNoticeStore(
    var inner: PrimaryRosterNoticeStore,
    var cache: TtlCache<PrimaryRosterNotice>,
    var hits: Int,
    var misses: Int,
) : PrimaryRosterNoticeStore {
    override fun find(code: String): PrimaryRosterNotice? {
        val hit = (cache.get(code) as PrimaryRosterNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryRosterNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryRosterNotice> = inner.list()
}

fun newCachedPrimaryRosterNoticeStore(inner: PrimaryRosterNoticeStore, ttl: Int, clock: Clock): PrimaryRosterNoticeStore {
    return CachedPrimaryRosterNoticeStore(
        inner = inner,
        cache = newTtlCache<PrimaryRosterNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary roster notices. Empty fields match all. */
data class PrimaryRosterNoticeQuery(
    var label: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryRosterNotice): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryRosterNotice>): MutableList<PrimaryRosterNotice> {
        val matched = filter(items) { candidate: PrimaryRosterNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryRosterNotices(store: PrimaryRosterNoticeStore, query: PrimaryRosterNoticeQuery): MutableList<PrimaryRosterNotice> {
    return query.apply(store.list())
}
