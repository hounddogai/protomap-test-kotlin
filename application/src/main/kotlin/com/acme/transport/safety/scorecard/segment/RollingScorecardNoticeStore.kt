package com.acme.transport.safety.scorecard.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling scorecard notices by code. */
interface RollingScorecardNoticeStore {
    fun find(code: String): RollingScorecardNotice?
    fun save(item: RollingScorecardNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingScorecardNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingScorecardNoticeStore(
    var items: MutableMap<String, RollingScorecardNotice>,
    var order: MutableList<String>,
) : RollingScorecardNoticeStore {
    override fun find(code: String): RollingScorecardNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingScorecardNotice) {
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

    override fun list(): MutableList<RollingScorecardNotice> {
        val out = mutableListOf<RollingScorecardNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingScorecardNoticeStore(): RollingScorecardNoticeStore {
    return MemoryRollingScorecardNoticeStore(
        mutableMapOf<String, RollingScorecardNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingScorecardNoticeStore(
    var inner: RollingScorecardNoticeStore,
    var cache: TtlCache<RollingScorecardNotice>,
    var hits: Int,
    var misses: Int,
) : RollingScorecardNoticeStore {
    override fun find(code: String): RollingScorecardNotice? {
        val hit = (cache.get(code) as RollingScorecardNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingScorecardNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingScorecardNotice> = inner.list()
}

fun newCachedRollingScorecardNoticeStore(inner: RollingScorecardNoticeStore, ttl: Int, clock: Clock): RollingScorecardNoticeStore {
    return CachedRollingScorecardNoticeStore(
        inner = inner,
        cache = newTtlCache<RollingScorecardNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling scorecard notices. Empty fields match all. */
data class RollingScorecardNoticeQuery(
    var title: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingScorecardNotice): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingScorecardNotice>): MutableList<RollingScorecardNotice> {
        val matched = filter(items) { candidate: RollingScorecardNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingScorecardNotices(store: RollingScorecardNoticeStore, query: RollingScorecardNoticeQuery): MutableList<RollingScorecardNotice> {
    return query.apply(store.list())
}
