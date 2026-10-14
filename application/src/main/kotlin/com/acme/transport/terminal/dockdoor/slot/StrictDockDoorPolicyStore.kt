package com.acme.transport.terminal.dockdoor.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps strict dock door policies by code. */
interface StrictDockDoorPolicyStore {
    fun find(code: String): StrictDockDoorPolicy?
    fun save(item: StrictDockDoorPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDockDoorPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDockDoorPolicyStore(
    var items: MutableMap<String, StrictDockDoorPolicy>,
    var order: MutableList<String>,
) : StrictDockDoorPolicyStore {
    override fun find(code: String): StrictDockDoorPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDockDoorPolicy) {
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

    override fun list(): MutableList<StrictDockDoorPolicy> {
        val out = mutableListOf<StrictDockDoorPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDockDoorPolicyStore(): StrictDockDoorPolicyStore {
    return MemoryStrictDockDoorPolicyStore(
        mutableMapOf<String, StrictDockDoorPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDockDoorPolicyStore(
    var inner: StrictDockDoorPolicyStore,
    var cache: TtlCache<StrictDockDoorPolicy>,
    var hits: Int,
    var misses: Int,
) : StrictDockDoorPolicyStore {
    override fun find(code: String): StrictDockDoorPolicy? {
        val hit = (cache.get(code) as StrictDockDoorPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDockDoorPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDockDoorPolicy> = inner.list()
}

fun newCachedStrictDockDoorPolicyStore(inner: StrictDockDoorPolicyStore, ttl: Int, clock: Clock): StrictDockDoorPolicyStore {
    return CachedStrictDockDoorPolicyStore(
        inner = inner,
        cache = newTtlCache<StrictDockDoorPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict dock door policies. Empty fields match all. */
data class StrictDockDoorPolicyQuery(
    var category: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDockDoorPolicy): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<StrictDockDoorPolicy>): MutableList<StrictDockDoorPolicy> {
        val matched = filter(items) { candidate: StrictDockDoorPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDockDoorPolicies(store: StrictDockDoorPolicyStore, query: StrictDockDoorPolicyQuery): MutableList<StrictDockDoorPolicy> {
    return query.apply(store.list())
}
