package com.acme.transport.safety.violation.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation claims by code. */
interface ViolationClaimStore {
    fun find(code: String): ViolationClaim?
    fun save(item: ViolationClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationClaimStore(
    var items: MutableMap<String, ViolationClaim>,
    var order: MutableList<String>,
) : ViolationClaimStore {
    override fun find(code: String): ViolationClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationClaim) {
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

    override fun list(): MutableList<ViolationClaim> {
        val out = mutableListOf<ViolationClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationClaimStore(): ViolationClaimStore {
    return MemoryViolationClaimStore(mutableMapOf<String, ViolationClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationClaimStore(
    var inner: ViolationClaimStore,
    var cache: TtlCache<ViolationClaim>,
    var hits: Int,
    var misses: Int,
) : ViolationClaimStore {
    override fun find(code: String): ViolationClaim? {
        val hit = (cache.get(code) as ViolationClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationClaim> = inner.list()
}

fun newCachedViolationClaimStore(inner: ViolationClaimStore, ttl: Int, clock: Clock): ViolationClaimStore {
    return CachedViolationClaimStore(
        inner = inner,
        cache = newTtlCache<ViolationClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation claims. Empty fields match all. */
data class ViolationClaimQuery(
    var unit: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationClaim): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<ViolationClaim>): MutableList<ViolationClaim> {
        val matched = filter(items) { candidate: ViolationClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationClaims(store: ViolationClaimStore, query: ViolationClaimQuery): MutableList<ViolationClaim> {
    return query.apply(store.list())
}
