package com.acme.transport.crewing.roster.rule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster scores by code. */
interface RosterScoreStore {
    fun find(code: String): RosterScore?
    fun save(item: RosterScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterScoreStore(
    var items: MutableMap<String, RosterScore>,
    var order: MutableList<String>,
) : RosterScoreStore {
    override fun find(code: String): RosterScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterScore) {
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

    override fun list(): MutableList<RosterScore> {
        val out = mutableListOf<RosterScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterScoreStore(): RosterScoreStore {
    return MemoryRosterScoreStore(mutableMapOf<String, RosterScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterScoreStore(
    var inner: RosterScoreStore,
    var cache: TtlCache<RosterScore>,
    var hits: Int,
    var misses: Int,
) : RosterScoreStore {
    override fun find(code: String): RosterScore? {
        val hit = (cache.get(code) as RosterScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterScore> = inner.list()
}

fun newCachedRosterScoreStore(inner: RosterScoreStore, ttl: Int, clock: Clock): RosterScoreStore {
    return CachedRosterScoreStore(
        inner = inner,
        cache = newTtlCache<RosterScore>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster scores. Empty fields match all. */
data class RosterScoreQuery(
    var label: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterScore): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<RosterScore>): MutableList<RosterScore> {
        val matched = filter(items) { candidate: RosterScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterScores(store: RosterScoreStore, query: RosterScoreQuery): MutableList<RosterScore> {
    return query.apply(store.list())
}
