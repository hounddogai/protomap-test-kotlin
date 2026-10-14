package com.acme.transport.safety.coaching.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching notices by code. */
interface CoachingNoticeStore {
    fun find(code: String): CoachingNotice?
    fun save(item: CoachingNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingNoticeStore(
    var items: MutableMap<String, CoachingNotice>,
    var order: MutableList<String>,
) : CoachingNoticeStore {
    override fun find(code: String): CoachingNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingNotice) {
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

    override fun list(): MutableList<CoachingNotice> {
        val out = mutableListOf<CoachingNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingNoticeStore(): CoachingNoticeStore {
    return MemoryCoachingNoticeStore(mutableMapOf<String, CoachingNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingNoticeStore(
    var inner: CoachingNoticeStore,
    var cache: TtlCache<CoachingNotice>,
    var hits: Int,
    var misses: Int,
) : CoachingNoticeStore {
    override fun find(code: String): CoachingNotice? {
        val hit = (cache.get(code) as CoachingNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingNotice> = inner.list()
}

fun newCachedCoachingNoticeStore(inner: CoachingNoticeStore, ttl: Int, clock: Clock): CoachingNoticeStore {
    return CachedCoachingNoticeStore(
        inner = inner,
        cache = newTtlCache<CoachingNotice>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching notices. Empty fields match all. */
data class CoachingNoticeQuery(
    var category: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingNotice): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingNotice>): MutableList<CoachingNotice> {
        val matched = filter(items) { candidate: CoachingNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingNotices(store: CoachingNoticeStore, query: CoachingNoticeQuery): MutableList<CoachingNotice> {
    return query.apply(store.list())
}
