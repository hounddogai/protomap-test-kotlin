package com.acme.transport.linehaul.relay.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lenient relay scores by code. */
interface LenientRelayScoreStore {
    fun find(code: String): LenientRelayScore?
    fun save(item: LenientRelayScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientRelayScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientRelayScoreStore(
    var items: MutableMap<String, LenientRelayScore>,
    var order: MutableList<String>,
) : LenientRelayScoreStore {
    override fun find(code: String): LenientRelayScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientRelayScore) {
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

    override fun list(): MutableList<LenientRelayScore> {
        val out = mutableListOf<LenientRelayScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientRelayScoreStore(): LenientRelayScoreStore {
    return MemoryLenientRelayScoreStore(mutableMapOf<String, LenientRelayScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientRelayScoreStore(
    var inner: LenientRelayScoreStore,
    var cache: TtlCache<LenientRelayScore>,
    var hits: Int,
    var misses: Int,
) : LenientRelayScoreStore {
    override fun find(code: String): LenientRelayScore? {
        val hit = (cache.get(code) as LenientRelayScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientRelayScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientRelayScore> = inner.list()
}

fun newCachedLenientRelayScoreStore(inner: LenientRelayScoreStore, ttl: Int, clock: Clock): LenientRelayScoreStore {
    return CachedLenientRelayScoreStore(
        inner = inner,
        cache = newTtlCache<LenientRelayScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient relay scores. Empty fields match all. */
data class LenientRelayScoreQuery(
    var label: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientRelayScore): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientRelayScore>): MutableList<LenientRelayScore> {
        val matched = filter(items) { candidate: LenientRelayScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientRelayScores(store: LenientRelayScoreStore, query: LenientRelayScoreQuery): MutableList<LenientRelayScore> {
    return query.apply(store.list())
}
