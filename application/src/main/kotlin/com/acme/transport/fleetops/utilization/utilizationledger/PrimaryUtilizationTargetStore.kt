package com.acme.transport.fleetops.utilization.utilizationledger

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary utilization targets by code. */
interface PrimaryUtilizationTargetStore {
    fun find(code: String): PrimaryUtilizationTarget?
    fun save(item: PrimaryUtilizationTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryUtilizationTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryUtilizationTargetStore(
    var items: MutableMap<String, PrimaryUtilizationTarget>,
    var order: MutableList<String>,
) : PrimaryUtilizationTargetStore {
    override fun find(code: String): PrimaryUtilizationTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryUtilizationTarget) {
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

    override fun list(): MutableList<PrimaryUtilizationTarget> {
        val out = mutableListOf<PrimaryUtilizationTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryUtilizationTargetStore(): PrimaryUtilizationTargetStore {
    return MemoryPrimaryUtilizationTargetStore(
        mutableMapOf<String, PrimaryUtilizationTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryUtilizationTargetStore(
    var inner: PrimaryUtilizationTargetStore,
    var cache: TtlCache<PrimaryUtilizationTarget>,
    var hits: Int,
    var misses: Int,
) : PrimaryUtilizationTargetStore {
    override fun find(code: String): PrimaryUtilizationTarget? {
        val hit = (cache.get(code) as PrimaryUtilizationTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryUtilizationTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryUtilizationTarget> = inner.list()
}

fun newCachedPrimaryUtilizationTargetStore(inner: PrimaryUtilizationTargetStore, ttl: Int, clock: Clock): PrimaryUtilizationTargetStore {
    return CachedPrimaryUtilizationTargetStore(
        inner = inner,
        cache = newTtlCache<PrimaryUtilizationTarget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary utilization targets. Empty fields match all. */
data class PrimaryUtilizationTargetQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryUtilizationTarget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryUtilizationTarget>): MutableList<PrimaryUtilizationTarget> {
        val matched = filter(items) { candidate: PrimaryUtilizationTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryUtilizationTargets(store: PrimaryUtilizationTargetStore, query: PrimaryUtilizationTargetQuery): MutableList<PrimaryUtilizationTarget> {
    return query.apply(store.list())
}
