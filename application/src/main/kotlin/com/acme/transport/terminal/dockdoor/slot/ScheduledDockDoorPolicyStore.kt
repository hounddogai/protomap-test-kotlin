package com.acme.transport.terminal.dockdoor.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled dock door policies by code. */
interface ScheduledDockDoorPolicyStore {
    fun find(code: String): ScheduledDockDoorPolicy?
    fun save(item: ScheduledDockDoorPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDockDoorPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDockDoorPolicyStore(
    var items: MutableMap<String, ScheduledDockDoorPolicy>,
    var order: MutableList<String>,
) : ScheduledDockDoorPolicyStore {
    override fun find(code: String): ScheduledDockDoorPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDockDoorPolicy) {
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

    override fun list(): MutableList<ScheduledDockDoorPolicy> {
        val out = mutableListOf<ScheduledDockDoorPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDockDoorPolicyStore(): ScheduledDockDoorPolicyStore {
    return MemoryScheduledDockDoorPolicyStore(
        mutableMapOf<String, ScheduledDockDoorPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDockDoorPolicyStore(
    var inner: ScheduledDockDoorPolicyStore,
    var cache: TtlCache<ScheduledDockDoorPolicy>,
    var hits: Int,
    var misses: Int,
) : ScheduledDockDoorPolicyStore {
    override fun find(code: String): ScheduledDockDoorPolicy? {
        val hit = (cache.get(code) as ScheduledDockDoorPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDockDoorPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDockDoorPolicy> = inner.list()
}

fun newCachedScheduledDockDoorPolicyStore(inner: ScheduledDockDoorPolicyStore, ttl: Int, clock: Clock): ScheduledDockDoorPolicyStore {
    return CachedScheduledDockDoorPolicyStore(
        inner = inner,
        cache = newTtlCache<ScheduledDockDoorPolicy>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled dock door policies. Empty fields match all. */
data class ScheduledDockDoorPolicyQuery(
    var segment: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDockDoorPolicy): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledDockDoorPolicy>): MutableList<ScheduledDockDoorPolicy> {
        val matched = filter(items) { candidate: ScheduledDockDoorPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDockDoorPolicies(store: ScheduledDockDoorPolicyStore, query: ScheduledDockDoorPolicyQuery): MutableList<ScheduledDockDoorPolicy> {
    return query.apply(store.list())
}
