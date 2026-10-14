package com.acme.transport.safety.scorecard.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps default scorecard notices by code. */
interface DefaultScorecardNoticeStore {
    fun find(code: String): DefaultScorecardNotice?
    fun save(item: DefaultScorecardNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultScorecardNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultScorecardNoticeStore(
    var items: MutableMap<String, DefaultScorecardNotice>,
    var order: MutableList<String>,
) : DefaultScorecardNoticeStore {
    override fun find(code: String): DefaultScorecardNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultScorecardNotice) {
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

    override fun list(): MutableList<DefaultScorecardNotice> {
        val out = mutableListOf<DefaultScorecardNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultScorecardNoticeStore(): DefaultScorecardNoticeStore {
    return MemoryDefaultScorecardNoticeStore(
        mutableMapOf<String, DefaultScorecardNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultScorecardNoticeStore(
    var inner: DefaultScorecardNoticeStore,
    var cache: TtlCache<DefaultScorecardNotice>,
    var hits: Int,
    var misses: Int,
) : DefaultScorecardNoticeStore {
    override fun find(code: String): DefaultScorecardNotice? {
        val hit = (cache.get(code) as DefaultScorecardNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultScorecardNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultScorecardNotice> = inner.list()
}

fun newCachedDefaultScorecardNoticeStore(inner: DefaultScorecardNoticeStore, ttl: Int, clock: Clock): DefaultScorecardNoticeStore {
    return CachedDefaultScorecardNoticeStore(
        inner = inner,
        cache = newTtlCache<DefaultScorecardNotice>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default scorecard notices. Empty fields match all. */
data class DefaultScorecardNoticeQuery(
    var depot: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultScorecardNotice): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<DefaultScorecardNotice>): MutableList<DefaultScorecardNotice> {
        val matched = filter(items) { candidate: DefaultScorecardNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultScorecardNotices(store: DefaultScorecardNoticeStore, query: DefaultScorecardNoticeQuery): MutableList<DefaultScorecardNotice> {
    return query.apply(store.list())
}
