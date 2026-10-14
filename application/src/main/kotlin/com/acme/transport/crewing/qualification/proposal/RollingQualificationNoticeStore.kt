package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling qualification notices by code. */
interface RollingQualificationNoticeStore {
    fun find(code: String): RollingQualificationNotice?
    fun save(item: RollingQualificationNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingQualificationNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingQualificationNoticeStore(
    var items: MutableMap<String, RollingQualificationNotice>,
    var order: MutableList<String>,
) : RollingQualificationNoticeStore {
    override fun find(code: String): RollingQualificationNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingQualificationNotice) {
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

    override fun list(): MutableList<RollingQualificationNotice> {
        val out = mutableListOf<RollingQualificationNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingQualificationNoticeStore(): RollingQualificationNoticeStore {
    return MemoryRollingQualificationNoticeStore(
        mutableMapOf<String, RollingQualificationNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingQualificationNoticeStore(
    var inner: RollingQualificationNoticeStore,
    var cache: TtlCache<RollingQualificationNotice>,
    var hits: Int,
    var misses: Int,
) : RollingQualificationNoticeStore {
    override fun find(code: String): RollingQualificationNotice? {
        val hit = (cache.get(code) as RollingQualificationNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingQualificationNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingQualificationNotice> = inner.list()
}

fun newCachedRollingQualificationNoticeStore(inner: RollingQualificationNoticeStore, ttl: Int, clock: Clock): RollingQualificationNoticeStore {
    return CachedRollingQualificationNoticeStore(
        inner = inner,
        cache = newTtlCache<RollingQualificationNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling qualification notices. Empty fields match all. */
data class RollingQualificationNoticeQuery(
    var locale: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingQualificationNotice): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingQualificationNotice>): MutableList<RollingQualificationNotice> {
        val matched = filter(items) { candidate: RollingQualificationNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingQualificationNotices(store: RollingQualificationNoticeStore, query: RollingQualificationNoticeQuery): MutableList<RollingQualificationNotice> {
    return query.apply(store.list())
}
