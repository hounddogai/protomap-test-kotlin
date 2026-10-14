package com.acme.transport.safety.scorecard.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard notices by code. */
interface ScorecardNoticeStore {
    fun find(code: String): ScorecardNotice?
    fun save(item: ScorecardNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardNoticeStore(
    var items: MutableMap<String, ScorecardNotice>,
    var order: MutableList<String>,
) : ScorecardNoticeStore {
    override fun find(code: String): ScorecardNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardNotice) {
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

    override fun list(): MutableList<ScorecardNotice> {
        val out = mutableListOf<ScorecardNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardNoticeStore(): ScorecardNoticeStore {
    return MemoryScorecardNoticeStore(mutableMapOf<String, ScorecardNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardNoticeStore(
    var inner: ScorecardNoticeStore,
    var cache: TtlCache<ScorecardNotice>,
    var hits: Int,
    var misses: Int,
) : ScorecardNoticeStore {
    override fun find(code: String): ScorecardNotice? {
        val hit = (cache.get(code) as ScorecardNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardNotice> = inner.list()
}

fun newCachedScorecardNoticeStore(inner: ScorecardNoticeStore, ttl: Int, clock: Clock): ScorecardNoticeStore {
    return CachedScorecardNoticeStore(
        inner = inner,
        cache = newTtlCache<ScorecardNotice>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard notices. Empty fields match all. */
data class ScorecardNoticeQuery(
    var label: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardNotice): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScorecardNotice>): MutableList<ScorecardNotice> {
        val matched = filter(items) { candidate: ScorecardNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardNotices(store: ScorecardNoticeStore, query: ScorecardNoticeQuery): MutableList<ScorecardNotice> {
    return query.apply(store.list())
}
