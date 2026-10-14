package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered rate card targets by code. */
interface TieredRateCardTargetStore {
    fun find(code: String): TieredRateCardTarget?
    fun save(item: TieredRateCardTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredRateCardTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredRateCardTargetStore(
    var items: MutableMap<String, TieredRateCardTarget>,
    var order: MutableList<String>,
) : TieredRateCardTargetStore {
    override fun find(code: String): TieredRateCardTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredRateCardTarget) {
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

    override fun list(): MutableList<TieredRateCardTarget> {
        val out = mutableListOf<TieredRateCardTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredRateCardTargetStore(): TieredRateCardTargetStore {
    return MemoryTieredRateCardTargetStore(
        mutableMapOf<String, TieredRateCardTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredRateCardTargetStore(
    var inner: TieredRateCardTargetStore,
    var cache: TtlCache<TieredRateCardTarget>,
    var hits: Int,
    var misses: Int,
) : TieredRateCardTargetStore {
    override fun find(code: String): TieredRateCardTarget? {
        val hit = (cache.get(code) as TieredRateCardTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredRateCardTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredRateCardTarget> = inner.list()
}

fun newCachedTieredRateCardTargetStore(inner: TieredRateCardTargetStore, ttl: Int, clock: Clock): TieredRateCardTargetStore {
    return CachedTieredRateCardTargetStore(
        inner = inner,
        cache = newTtlCache<TieredRateCardTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered rate card targets. Empty fields match all. */
data class TieredRateCardTargetQuery(
    var depot: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredRateCardTarget): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredRateCardTarget>): MutableList<TieredRateCardTarget> {
        val matched = filter(items) { candidate: TieredRateCardTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredRateCardTargets(store: TieredRateCardTargetStore, query: TieredRateCardTargetQuery): MutableList<TieredRateCardTarget> {
    return query.apply(store.list())
}
