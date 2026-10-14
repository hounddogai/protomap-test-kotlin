package com.acme.transport.fleetops.utilization.utilizationsummary

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback utilization targets by code. */
interface FallbackUtilizationTargetStore {
    fun find(code: String): FallbackUtilizationTarget?
    fun save(item: FallbackUtilizationTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackUtilizationTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackUtilizationTargetStore(
    var items: MutableMap<String, FallbackUtilizationTarget>,
    var order: MutableList<String>,
) : FallbackUtilizationTargetStore {
    override fun find(code: String): FallbackUtilizationTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackUtilizationTarget) {
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

    override fun list(): MutableList<FallbackUtilizationTarget> {
        val out = mutableListOf<FallbackUtilizationTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackUtilizationTargetStore(): FallbackUtilizationTargetStore {
    return MemoryFallbackUtilizationTargetStore(
        mutableMapOf<String, FallbackUtilizationTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackUtilizationTargetStore(
    var inner: FallbackUtilizationTargetStore,
    var cache: TtlCache<FallbackUtilizationTarget>,
    var hits: Int,
    var misses: Int,
) : FallbackUtilizationTargetStore {
    override fun find(code: String): FallbackUtilizationTarget? {
        val hit = (cache.get(code) as FallbackUtilizationTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackUtilizationTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackUtilizationTarget> = inner.list()
}

fun newCachedFallbackUtilizationTargetStore(inner: FallbackUtilizationTargetStore, ttl: Int, clock: Clock): FallbackUtilizationTargetStore {
    return CachedFallbackUtilizationTargetStore(
        inner = inner,
        cache = newTtlCache<FallbackUtilizationTarget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback utilization targets. Empty fields match all. */
data class FallbackUtilizationTargetQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackUtilizationTarget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackUtilizationTarget>): MutableList<FallbackUtilizationTarget> {
        val matched = filter(items) { candidate: FallbackUtilizationTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackUtilizationTargets(store: FallbackUtilizationTargetStore, query: FallbackUtilizationTargetQuery): MutableList<FallbackUtilizationTarget> {
    return query.apply(store.list())
}
