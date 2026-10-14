package com.acme.transport.emissions.evcharge.segment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending ev charge overrides by code. */
interface PendingEvChargeOverrideStore {
    fun find(code: String): PendingEvChargeOverride?
    fun save(item: PendingEvChargeOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingEvChargeOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingEvChargeOverrideStore(
    var items: MutableMap<String, PendingEvChargeOverride>,
    var order: MutableList<String>,
) : PendingEvChargeOverrideStore {
    override fun find(code: String): PendingEvChargeOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingEvChargeOverride) {
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

    override fun list(): MutableList<PendingEvChargeOverride> {
        val out = mutableListOf<PendingEvChargeOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingEvChargeOverrideStore(): PendingEvChargeOverrideStore {
    return MemoryPendingEvChargeOverrideStore(
        mutableMapOf<String, PendingEvChargeOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingEvChargeOverrideStore(
    var inner: PendingEvChargeOverrideStore,
    var cache: TtlCache<PendingEvChargeOverride>,
    var hits: Int,
    var misses: Int,
) : PendingEvChargeOverrideStore {
    override fun find(code: String): PendingEvChargeOverride? {
        val hit = (cache.get(code) as PendingEvChargeOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingEvChargeOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingEvChargeOverride> = inner.list()
}

fun newCachedPendingEvChargeOverrideStore(inner: PendingEvChargeOverrideStore, ttl: Int, clock: Clock): PendingEvChargeOverrideStore {
    return CachedPendingEvChargeOverrideStore(
        inner = inner,
        cache = newTtlCache<PendingEvChargeOverride>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending ev charge overrides. Empty fields match all. */
data class PendingEvChargeOverrideQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingEvChargeOverride): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingEvChargeOverride>): MutableList<PendingEvChargeOverride> {
        val matched = filter(items) { candidate: PendingEvChargeOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingEvChargeOverrides(store: PendingEvChargeOverrideStore, query: PendingEvChargeOverrideQuery): MutableList<PendingEvChargeOverride> {
    return query.apply(store.list())
}
