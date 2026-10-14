package com.acme.transport.terminal.dockdoor.dooragreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door policies by code. */
interface DockDoorPolicyStore {
    fun find(code: String): DockDoorPolicy?
    fun save(item: DockDoorPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorPolicyStore(
    var items: MutableMap<String, DockDoorPolicy>,
    var order: MutableList<String>,
) : DockDoorPolicyStore {
    override fun find(code: String): DockDoorPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorPolicy) {
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

    override fun list(): MutableList<DockDoorPolicy> {
        val out = mutableListOf<DockDoorPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorPolicyStore(): DockDoorPolicyStore {
    return MemoryDockDoorPolicyStore(mutableMapOf<String, DockDoorPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorPolicyStore(
    var inner: DockDoorPolicyStore,
    var cache: TtlCache<DockDoorPolicy>,
    var hits: Int,
    var misses: Int,
) : DockDoorPolicyStore {
    override fun find(code: String): DockDoorPolicy? {
        val hit = (cache.get(code) as DockDoorPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorPolicy> = inner.list()
}

fun newCachedDockDoorPolicyStore(inner: DockDoorPolicyStore, ttl: Int, clock: Clock): DockDoorPolicyStore {
    return CachedDockDoorPolicyStore(
        inner = inner,
        cache = newTtlCache<DockDoorPolicy>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door policies. Empty fields match all. */
data class DockDoorPolicyQuery(
    var depot: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorPolicy): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorPolicy>): MutableList<DockDoorPolicy> {
        val matched = filter(items) { candidate: DockDoorPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorPolicies(store: DockDoorPolicyStore, query: DockDoorPolicyQuery): MutableList<DockDoorPolicy> {
    return query.apply(store.list())
}
