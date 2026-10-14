package com.acme.transport.safety.coaching.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps manual coaching notices by code. */
interface ManualCoachingNoticeStore {
    fun find(code: String): ManualCoachingNotice?
    fun save(item: ManualCoachingNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualCoachingNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualCoachingNoticeStore(
    var items: MutableMap<String, ManualCoachingNotice>,
    var order: MutableList<String>,
) : ManualCoachingNoticeStore {
    override fun find(code: String): ManualCoachingNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualCoachingNotice) {
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

    override fun list(): MutableList<ManualCoachingNotice> {
        val out = mutableListOf<ManualCoachingNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualCoachingNoticeStore(): ManualCoachingNoticeStore {
    return MemoryManualCoachingNoticeStore(
        mutableMapOf<String, ManualCoachingNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualCoachingNoticeStore(
    var inner: ManualCoachingNoticeStore,
    var cache: TtlCache<ManualCoachingNotice>,
    var hits: Int,
    var misses: Int,
) : ManualCoachingNoticeStore {
    override fun find(code: String): ManualCoachingNotice? {
        val hit = (cache.get(code) as ManualCoachingNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualCoachingNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualCoachingNotice> = inner.list()
}

fun newCachedManualCoachingNoticeStore(inner: ManualCoachingNoticeStore, ttl: Int, clock: Clock): ManualCoachingNoticeStore {
    return CachedManualCoachingNoticeStore(
        inner = inner,
        cache = newTtlCache<ManualCoachingNotice>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual coaching notices. Empty fields match all. */
data class ManualCoachingNoticeQuery(
    var depot: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualCoachingNotice): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<ManualCoachingNotice>): MutableList<ManualCoachingNotice> {
        val matched = filter(items) { candidate: ManualCoachingNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualCoachingNotices(store: ManualCoachingNoticeStore, query: ManualCoachingNoticeQuery): MutableList<ManualCoachingNotice> {
    return query.apply(store.list())
}
