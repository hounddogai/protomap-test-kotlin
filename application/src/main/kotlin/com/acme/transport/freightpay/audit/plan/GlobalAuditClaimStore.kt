package com.acme.transport.freightpay.audit.plan

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global audit claims by code. */
interface GlobalAuditClaimStore {
    fun find(code: String): GlobalAuditClaim?
    fun save(item: GlobalAuditClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalAuditClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalAuditClaimStore(
    var items: MutableMap<String, GlobalAuditClaim>,
    var order: MutableList<String>,
) : GlobalAuditClaimStore {
    override fun find(code: String): GlobalAuditClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalAuditClaim) {
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

    override fun list(): MutableList<GlobalAuditClaim> {
        val out = mutableListOf<GlobalAuditClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalAuditClaimStore(): GlobalAuditClaimStore {
    return MemoryGlobalAuditClaimStore(mutableMapOf<String, GlobalAuditClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalAuditClaimStore(
    var inner: GlobalAuditClaimStore,
    var cache: TtlCache<GlobalAuditClaim>,
    var hits: Int,
    var misses: Int,
) : GlobalAuditClaimStore {
    override fun find(code: String): GlobalAuditClaim? {
        val hit = (cache.get(code) as GlobalAuditClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalAuditClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalAuditClaim> = inner.list()
}

fun newCachedGlobalAuditClaimStore(inner: GlobalAuditClaimStore, ttl: Int, clock: Clock): GlobalAuditClaimStore {
    return CachedGlobalAuditClaimStore(
        inner = inner,
        cache = newTtlCache<GlobalAuditClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global audit claims. Empty fields match all. */
data class GlobalAuditClaimQuery(
    var channel: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalAuditClaim): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalAuditClaim>): MutableList<GlobalAuditClaim> {
        val matched = filter(items) { candidate: GlobalAuditClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalAuditClaims(store: GlobalAuditClaimStore, query: GlobalAuditClaimQuery): MutableList<GlobalAuditClaim> {
    return query.apply(store.list())
}
