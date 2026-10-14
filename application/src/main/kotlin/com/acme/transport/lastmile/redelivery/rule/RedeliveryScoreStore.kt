package com.acme.transport.lastmile.redelivery.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery scores by code. */
interface RedeliveryScoreStore {
    fun find(code: String): RedeliveryScore?
    fun save(item: RedeliveryScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryScoreStore(
    var items: MutableMap<String, RedeliveryScore>,
    var order: MutableList<String>,
) : RedeliveryScoreStore {
    override fun find(code: String): RedeliveryScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryScore) {
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

    override fun list(): MutableList<RedeliveryScore> {
        val out = mutableListOf<RedeliveryScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryScoreStore(): RedeliveryScoreStore {
    return MemoryRedeliveryScoreStore(mutableMapOf<String, RedeliveryScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryScoreStore(
    var inner: RedeliveryScoreStore,
    var cache: TtlCache<RedeliveryScore>,
    var hits: Int,
    var misses: Int,
) : RedeliveryScoreStore {
    override fun find(code: String): RedeliveryScore? {
        val hit = (cache.get(code) as RedeliveryScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryScore> = inner.list()
}

fun newCachedRedeliveryScoreStore(inner: RedeliveryScoreStore, ttl: Int, clock: Clock): RedeliveryScoreStore {
    return CachedRedeliveryScoreStore(
        inner = inner,
        cache = newTtlCache<RedeliveryScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery scores. Empty fields match all. */
data class RedeliveryScoreQuery(
    var title: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryScore): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliveryScore>): MutableList<RedeliveryScore> {
        val matched = filter(items) { candidate: RedeliveryScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryScores(store: RedeliveryScoreStore, query: RedeliveryScoreQuery): MutableList<RedeliveryScore> {
    return query.apply(store.list())
}
