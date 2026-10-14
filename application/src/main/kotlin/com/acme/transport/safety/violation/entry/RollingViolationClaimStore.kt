package com.acme.transport.safety.violation.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling violation claims by code. */
interface RollingViolationClaimStore {
    fun find(code: String): RollingViolationClaim?
    fun save(item: RollingViolationClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingViolationClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingViolationClaimStore(
    var items: MutableMap<String, RollingViolationClaim>,
    var order: MutableList<String>,
) : RollingViolationClaimStore {
    override fun find(code: String): RollingViolationClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingViolationClaim) {
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

    override fun list(): MutableList<RollingViolationClaim> {
        val out = mutableListOf<RollingViolationClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingViolationClaimStore(): RollingViolationClaimStore {
    return MemoryRollingViolationClaimStore(
        mutableMapOf<String, RollingViolationClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingViolationClaimStore(
    var inner: RollingViolationClaimStore,
    var cache: TtlCache<RollingViolationClaim>,
    var hits: Int,
    var misses: Int,
) : RollingViolationClaimStore {
    override fun find(code: String): RollingViolationClaim? {
        val hit = (cache.get(code) as RollingViolationClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingViolationClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingViolationClaim> = inner.list()
}

fun newCachedRollingViolationClaimStore(inner: RollingViolationClaimStore, ttl: Int, clock: Clock): RollingViolationClaimStore {
    return CachedRollingViolationClaimStore(
        inner = inner,
        cache = newTtlCache<RollingViolationClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling violation claims. Empty fields match all. */
data class RollingViolationClaimQuery(
    var title: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingViolationClaim): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<RollingViolationClaim>): MutableList<RollingViolationClaim> {
        val matched = filter(items) { candidate: RollingViolationClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingViolationClaims(store: RollingViolationClaimStore, query: RollingViolationClaimQuery): MutableList<RollingViolationClaim> {
    return query.apply(store.list())
}
