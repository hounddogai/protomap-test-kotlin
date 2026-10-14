package com.acme.transport.crewing.qualification.tieredqualificationdigest

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional qualification notices by code. */
interface RegionalQualificationNoticeStore {
    fun find(code: String): RegionalQualificationNotice?
    fun save(item: RegionalQualificationNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalQualificationNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalQualificationNoticeStore(
    var items: MutableMap<String, RegionalQualificationNotice>,
    var order: MutableList<String>,
) : RegionalQualificationNoticeStore {
    override fun find(code: String): RegionalQualificationNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalQualificationNotice) {
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

    override fun list(): MutableList<RegionalQualificationNotice> {
        val out = mutableListOf<RegionalQualificationNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalQualificationNoticeStore(): RegionalQualificationNoticeStore {
    return MemoryRegionalQualificationNoticeStore(
        mutableMapOf<String, RegionalQualificationNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalQualificationNoticeStore(
    var inner: RegionalQualificationNoticeStore,
    var cache: TtlCache<RegionalQualificationNotice>,
    var hits: Int,
    var misses: Int,
) : RegionalQualificationNoticeStore {
    override fun find(code: String): RegionalQualificationNotice? {
        val hit = (cache.get(code) as RegionalQualificationNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalQualificationNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalQualificationNotice> = inner.list()
}

fun newCachedRegionalQualificationNoticeStore(inner: RegionalQualificationNoticeStore, ttl: Int, clock: Clock): RegionalQualificationNoticeStore {
    return CachedRegionalQualificationNoticeStore(
        inner = inner,
        cache = newTtlCache<RegionalQualificationNotice>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional qualification notices. Empty fields match all. */
data class RegionalQualificationNoticeQuery(
    var title: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalQualificationNotice): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<RegionalQualificationNotice>): MutableList<RegionalQualificationNotice> {
        val matched = filter(items) { candidate: RegionalQualificationNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalQualificationNotices(store: RegionalQualificationNoticeStore, query: RegionalQualificationNoticeQuery): MutableList<RegionalQualificationNotice> {
    return query.apply(store.list())
}
