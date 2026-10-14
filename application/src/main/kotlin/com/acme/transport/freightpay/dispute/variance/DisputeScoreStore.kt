package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute scores by code. */
interface DisputeScoreStore {
    fun find(code: String): DisputeScore?
    fun save(item: DisputeScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeScoreStore(
    var items: MutableMap<String, DisputeScore>,
    var order: MutableList<String>,
) : DisputeScoreStore {
    override fun find(code: String): DisputeScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeScore) {
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

    override fun list(): MutableList<DisputeScore> {
        val out = mutableListOf<DisputeScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeScoreStore(): DisputeScoreStore {
    return MemoryDisputeScoreStore(mutableMapOf<String, DisputeScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeScoreStore(
    var inner: DisputeScoreStore,
    var cache: TtlCache<DisputeScore>,
    var hits: Int,
    var misses: Int,
) : DisputeScoreStore {
    override fun find(code: String): DisputeScore? {
        val hit = (cache.get(code) as DisputeScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeScore> = inner.list()
}

fun newCachedDisputeScoreStore(inner: DisputeScoreStore, ttl: Int, clock: Clock): DisputeScoreStore {
    return CachedDisputeScoreStore(
        inner = inner,
        cache = newTtlCache<DisputeScore>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute scores. Empty fields match all. */
data class DisputeScoreQuery(
    var unit: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeScore): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeScore>): MutableList<DisputeScore> {
        val matched = filter(items) { candidate: DisputeScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeScores(store: DisputeScoreStore, query: DisputeScoreQuery): MutableList<DisputeScore> {
    return query.apply(store.list())
}
