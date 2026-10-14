package com.acme.transport.fleetops.utilization.target

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization targets by code. */
interface UtilizationTargetStore {
    fun find(code: String): UtilizationTarget?
    fun save(item: UtilizationTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationTargetStore(
    var items: MutableMap<String, UtilizationTarget>,
    var order: MutableList<String>,
) : UtilizationTargetStore {
    override fun find(code: String): UtilizationTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationTarget) {
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

    override fun list(): MutableList<UtilizationTarget> {
        val out = mutableListOf<UtilizationTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationTargetStore(): UtilizationTargetStore {
    return MemoryUtilizationTargetStore(mutableMapOf<String, UtilizationTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationTargetStore(
    var inner: UtilizationTargetStore,
    var cache: TtlCache<UtilizationTarget>,
    var hits: Int,
    var misses: Int,
) : UtilizationTargetStore {
    override fun find(code: String): UtilizationTarget? {
        val hit = (cache.get(code) as UtilizationTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationTarget> = inner.list()
}

fun newCachedUtilizationTargetStore(inner: UtilizationTargetStore, ttl: Int, clock: Clock): UtilizationTargetStore {
    return CachedUtilizationTargetStore(
        inner = inner,
        cache = newTtlCache<UtilizationTarget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization targets. Empty fields match all. */
data class UtilizationTargetQuery(
    var source: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationTarget): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<UtilizationTarget>): MutableList<UtilizationTarget> {
        val matched = filter(items) { candidate: UtilizationTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationTargets(store: UtilizationTargetStore, query: UtilizationTargetQuery): MutableList<UtilizationTarget> {
    return query.apply(store.list())
}
