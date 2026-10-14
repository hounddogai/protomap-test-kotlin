package com.acme.transport.freightpay.dispute.disputeforecast

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute targets by code. */
interface DisputeTargetStore {
    fun find(code: String): DisputeTarget?
    fun save(item: DisputeTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeTargetStore(
    var items: MutableMap<String, DisputeTarget>,
    var order: MutableList<String>,
) : DisputeTargetStore {
    override fun find(code: String): DisputeTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeTarget) {
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

    override fun list(): MutableList<DisputeTarget> {
        val out = mutableListOf<DisputeTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeTargetStore(): DisputeTargetStore {
    return MemoryDisputeTargetStore(mutableMapOf<String, DisputeTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeTargetStore(
    var inner: DisputeTargetStore,
    var cache: TtlCache<DisputeTarget>,
    var hits: Int,
    var misses: Int,
) : DisputeTargetStore {
    override fun find(code: String): DisputeTarget? {
        val hit = (cache.get(code) as DisputeTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeTarget> = inner.list()
}

fun newCachedDisputeTargetStore(inner: DisputeTargetStore, ttl: Int, clock: Clock): DisputeTargetStore {
    return CachedDisputeTargetStore(
        inner = inner,
        cache = newTtlCache<DisputeTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute targets. Empty fields match all. */
data class DisputeTargetQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeTarget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeTarget>): MutableList<DisputeTarget> {
        val matched = filter(items) { candidate: DisputeTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeTargets(store: DisputeTargetStore, query: DisputeTargetQuery): MutableList<DisputeTarget> {
    return query.apply(store.list())
}
