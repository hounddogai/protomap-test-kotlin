package com.acme.transport.terminal.dockdoor.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted dock door claims by code. */
interface WeightedDockDoorClaimStore {
    fun find(code: String): WeightedDockDoorClaim?
    fun save(item: WeightedDockDoorClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDockDoorClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDockDoorClaimStore(
    var items: MutableMap<String, WeightedDockDoorClaim>,
    var order: MutableList<String>,
) : WeightedDockDoorClaimStore {
    override fun find(code: String): WeightedDockDoorClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDockDoorClaim) {
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

    override fun list(): MutableList<WeightedDockDoorClaim> {
        val out = mutableListOf<WeightedDockDoorClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDockDoorClaimStore(): WeightedDockDoorClaimStore {
    return MemoryWeightedDockDoorClaimStore(
        mutableMapOf<String, WeightedDockDoorClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDockDoorClaimStore(
    var inner: WeightedDockDoorClaimStore,
    var cache: TtlCache<WeightedDockDoorClaim>,
    var hits: Int,
    var misses: Int,
) : WeightedDockDoorClaimStore {
    override fun find(code: String): WeightedDockDoorClaim? {
        val hit = (cache.get(code) as WeightedDockDoorClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDockDoorClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDockDoorClaim> = inner.list()
}

fun newCachedWeightedDockDoorClaimStore(inner: WeightedDockDoorClaimStore, ttl: Int, clock: Clock): WeightedDockDoorClaimStore {
    return CachedWeightedDockDoorClaimStore(
        inner = inner,
        cache = newTtlCache<WeightedDockDoorClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted dock door claims. Empty fields match all. */
data class WeightedDockDoorClaimQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDockDoorClaim): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedDockDoorClaim>): MutableList<WeightedDockDoorClaim> {
        val matched = filter(items) { candidate: WeightedDockDoorClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDockDoorClaims(store: WeightedDockDoorClaimStore, query: WeightedDockDoorClaimQuery): MutableList<WeightedDockDoorClaim> {
    return query.apply(store.list())
}
