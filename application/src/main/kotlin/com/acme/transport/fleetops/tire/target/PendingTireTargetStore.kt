package com.acme.transport.fleetops.tire.target

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending tire targets by code. */
interface PendingTireTargetStore {
    fun find(code: String): PendingTireTarget?
    fun save(item: PendingTireTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingTireTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingTireTargetStore(
    var items: MutableMap<String, PendingTireTarget>,
    var order: MutableList<String>,
) : PendingTireTargetStore {
    override fun find(code: String): PendingTireTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingTireTarget) {
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

    override fun list(): MutableList<PendingTireTarget> {
        val out = mutableListOf<PendingTireTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingTireTargetStore(): PendingTireTargetStore {
    return MemoryPendingTireTargetStore(mutableMapOf<String, PendingTireTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingTireTargetStore(
    var inner: PendingTireTargetStore,
    var cache: TtlCache<PendingTireTarget>,
    var hits: Int,
    var misses: Int,
) : PendingTireTargetStore {
    override fun find(code: String): PendingTireTarget? {
        val hit = (cache.get(code) as PendingTireTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingTireTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingTireTarget> = inner.list()
}

fun newCachedPendingTireTargetStore(inner: PendingTireTargetStore, ttl: Int, clock: Clock): PendingTireTargetStore {
    return CachedPendingTireTargetStore(
        inner = inner,
        cache = newTtlCache<PendingTireTarget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending tire targets. Empty fields match all. */
data class PendingTireTargetQuery(
    var label: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingTireTarget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingTireTarget>): MutableList<PendingTireTarget> {
        val matched = filter(items) { candidate: PendingTireTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingTireTargets(store: PendingTireTargetStore, query: PendingTireTargetQuery): MutableList<PendingTireTarget> {
    return query.apply(store.list())
}
