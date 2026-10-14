package com.acme.transport.freightpay.audit.auditclaim

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling audit claims by code. */
interface RollingAuditClaimStore {
    fun find(code: String): RollingAuditClaim?
    fun save(item: RollingAuditClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingAuditClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingAuditClaimStore(
    var items: MutableMap<String, RollingAuditClaim>,
    var order: MutableList<String>,
) : RollingAuditClaimStore {
    override fun find(code: String): RollingAuditClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingAuditClaim) {
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

    override fun list(): MutableList<RollingAuditClaim> {
        val out = mutableListOf<RollingAuditClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingAuditClaimStore(): RollingAuditClaimStore {
    return MemoryRollingAuditClaimStore(mutableMapOf<String, RollingAuditClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingAuditClaimStore(
    var inner: RollingAuditClaimStore,
    var cache: TtlCache<RollingAuditClaim>,
    var hits: Int,
    var misses: Int,
) : RollingAuditClaimStore {
    override fun find(code: String): RollingAuditClaim? {
        val hit = (cache.get(code) as RollingAuditClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingAuditClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingAuditClaim> = inner.list()
}

fun newCachedRollingAuditClaimStore(inner: RollingAuditClaimStore, ttl: Int, clock: Clock): RollingAuditClaimStore {
    return CachedRollingAuditClaimStore(
        inner = inner,
        cache = newTtlCache<RollingAuditClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling audit claims. Empty fields match all. */
data class RollingAuditClaimQuery(
    var lane: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingAuditClaim): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingAuditClaim>): MutableList<RollingAuditClaim> {
        val matched = filter(items) { candidate: RollingAuditClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingAuditClaims(store: RollingAuditClaimStore, query: RollingAuditClaimQuery): MutableList<RollingAuditClaim> {
    return query.apply(store.list())
}
