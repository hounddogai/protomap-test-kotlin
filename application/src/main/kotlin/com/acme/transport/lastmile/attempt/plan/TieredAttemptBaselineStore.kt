package com.acme.transport.lastmile.attempt.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps tiered attempt baselines by code. */
interface TieredAttemptBaselineStore {
    fun find(code: String): TieredAttemptBaseline?
    fun save(item: TieredAttemptBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredAttemptBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredAttemptBaselineStore(
    var items: MutableMap<String, TieredAttemptBaseline>,
    var order: MutableList<String>,
) : TieredAttemptBaselineStore {
    override fun find(code: String): TieredAttemptBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredAttemptBaseline) {
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

    override fun list(): MutableList<TieredAttemptBaseline> {
        val out = mutableListOf<TieredAttemptBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredAttemptBaselineStore(): TieredAttemptBaselineStore {
    return MemoryTieredAttemptBaselineStore(
        mutableMapOf<String, TieredAttemptBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredAttemptBaselineStore(
    var inner: TieredAttemptBaselineStore,
    var cache: TtlCache<TieredAttemptBaseline>,
    var hits: Int,
    var misses: Int,
) : TieredAttemptBaselineStore {
    override fun find(code: String): TieredAttemptBaseline? {
        val hit = (cache.get(code) as TieredAttemptBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredAttemptBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredAttemptBaseline> = inner.list()
}

fun newCachedTieredAttemptBaselineStore(inner: TieredAttemptBaselineStore, ttl: Int, clock: Clock): TieredAttemptBaselineStore {
    return CachedTieredAttemptBaselineStore(
        inner = inner,
        cache = newTtlCache<TieredAttemptBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered attempt baselines. Empty fields match all. */
data class TieredAttemptBaselineQuery(
    var lane: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredAttemptBaseline): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<TieredAttemptBaseline>): MutableList<TieredAttemptBaseline> {
        val matched = filter(items) { candidate: TieredAttemptBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredAttemptBaselines(store: TieredAttemptBaselineStore, query: TieredAttemptBaselineQuery): MutableList<TieredAttemptBaseline> {
    return query.apply(store.list())
}
