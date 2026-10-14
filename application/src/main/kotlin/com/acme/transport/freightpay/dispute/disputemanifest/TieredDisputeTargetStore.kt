package com.acme.transport.freightpay.dispute.disputemanifest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered dispute targets by code. */
interface TieredDisputeTargetStore {
    fun find(code: String): TieredDisputeTarget?
    fun save(item: TieredDisputeTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDisputeTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDisputeTargetStore(
    var items: MutableMap<String, TieredDisputeTarget>,
    var order: MutableList<String>,
) : TieredDisputeTargetStore {
    override fun find(code: String): TieredDisputeTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDisputeTarget) {
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

    override fun list(): MutableList<TieredDisputeTarget> {
        val out = mutableListOf<TieredDisputeTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDisputeTargetStore(): TieredDisputeTargetStore {
    return MemoryTieredDisputeTargetStore(mutableMapOf<String, TieredDisputeTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDisputeTargetStore(
    var inner: TieredDisputeTargetStore,
    var cache: TtlCache<TieredDisputeTarget>,
    var hits: Int,
    var misses: Int,
) : TieredDisputeTargetStore {
    override fun find(code: String): TieredDisputeTarget? {
        val hit = (cache.get(code) as TieredDisputeTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDisputeTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDisputeTarget> = inner.list()
}

fun newCachedTieredDisputeTargetStore(inner: TieredDisputeTargetStore, ttl: Int, clock: Clock): TieredDisputeTargetStore {
    return CachedTieredDisputeTargetStore(
        inner = inner,
        cache = newTtlCache<TieredDisputeTarget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered dispute targets. Empty fields match all. */
data class TieredDisputeTargetQuery(
    var unit: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDisputeTarget): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredDisputeTarget>): MutableList<TieredDisputeTarget> {
        val matched = filter(items) { candidate: TieredDisputeTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDisputeTargets(store: TieredDisputeTargetStore, query: TieredDisputeTargetQuery): MutableList<TieredDisputeTarget> {
    return query.apply(store.list())
}
