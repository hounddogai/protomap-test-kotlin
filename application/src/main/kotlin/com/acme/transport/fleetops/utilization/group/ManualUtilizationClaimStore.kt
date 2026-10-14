package com.acme.transport.fleetops.utilization.group

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual utilization claims by code. */
interface ManualUtilizationClaimStore {
    fun find(code: String): ManualUtilizationClaim?
    fun save(item: ManualUtilizationClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualUtilizationClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualUtilizationClaimStore(
    var items: MutableMap<String, ManualUtilizationClaim>,
    var order: MutableList<String>,
) : ManualUtilizationClaimStore {
    override fun find(code: String): ManualUtilizationClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualUtilizationClaim) {
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

    override fun list(): MutableList<ManualUtilizationClaim> {
        val out = mutableListOf<ManualUtilizationClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualUtilizationClaimStore(): ManualUtilizationClaimStore {
    return MemoryManualUtilizationClaimStore(
        mutableMapOf<String, ManualUtilizationClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualUtilizationClaimStore(
    var inner: ManualUtilizationClaimStore,
    var cache: TtlCache<ManualUtilizationClaim>,
    var hits: Int,
    var misses: Int,
) : ManualUtilizationClaimStore {
    override fun find(code: String): ManualUtilizationClaim? {
        val hit = (cache.get(code) as ManualUtilizationClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualUtilizationClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualUtilizationClaim> = inner.list()
}

fun newCachedManualUtilizationClaimStore(inner: ManualUtilizationClaimStore, ttl: Int, clock: Clock): ManualUtilizationClaimStore {
    return CachedManualUtilizationClaimStore(
        inner = inner,
        cache = newTtlCache<ManualUtilizationClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual utilization claims. Empty fields match all. */
data class ManualUtilizationClaimQuery(
    var channel: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualUtilizationClaim): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<ManualUtilizationClaim>): MutableList<ManualUtilizationClaim> {
        val matched = filter(items) { candidate: ManualUtilizationClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualUtilizationClaims(store: ManualUtilizationClaimStore, query: ManualUtilizationClaimQuery): MutableList<ManualUtilizationClaim> {
    return query.apply(store.list())
}
