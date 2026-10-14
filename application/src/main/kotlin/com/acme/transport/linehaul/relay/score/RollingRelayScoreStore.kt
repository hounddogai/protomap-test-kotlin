package com.acme.transport.linehaul.relay.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps rolling relay scores by code. */
interface RollingRelayScoreStore {
    fun find(code: String): RollingRelayScore?
    fun save(item: RollingRelayScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingRelayScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingRelayScoreStore(
    var items: MutableMap<String, RollingRelayScore>,
    var order: MutableList<String>,
) : RollingRelayScoreStore {
    override fun find(code: String): RollingRelayScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingRelayScore) {
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

    override fun list(): MutableList<RollingRelayScore> {
        val out = mutableListOf<RollingRelayScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingRelayScoreStore(): RollingRelayScoreStore {
    return MemoryRollingRelayScoreStore(mutableMapOf<String, RollingRelayScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingRelayScoreStore(
    var inner: RollingRelayScoreStore,
    var cache: TtlCache<RollingRelayScore>,
    var hits: Int,
    var misses: Int,
) : RollingRelayScoreStore {
    override fun find(code: String): RollingRelayScore? {
        val hit = (cache.get(code) as RollingRelayScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingRelayScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingRelayScore> = inner.list()
}

fun newCachedRollingRelayScoreStore(inner: RollingRelayScoreStore, ttl: Int, clock: Clock): RollingRelayScoreStore {
    return CachedRollingRelayScoreStore(
        inner = inner,
        cache = newTtlCache<RollingRelayScore>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling relay scores. Empty fields match all. */
data class RollingRelayScoreQuery(
    var name: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingRelayScore): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<RollingRelayScore>): MutableList<RollingRelayScore> {
        val matched = filter(items) { candidate: RollingRelayScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingRelayScores(store: RollingRelayScoreStore, query: RollingRelayScoreQuery): MutableList<RollingRelayScore> {
    return query.apply(store.list())
}
