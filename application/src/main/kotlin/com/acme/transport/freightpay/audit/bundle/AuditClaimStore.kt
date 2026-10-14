package com.acme.transport.freightpay.audit.bundle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit claims by code. */
interface AuditClaimStore {
    fun find(code: String): AuditClaim?
    fun save(item: AuditClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditClaimStore(
    var items: MutableMap<String, AuditClaim>,
    var order: MutableList<String>,
) : AuditClaimStore {
    override fun find(code: String): AuditClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditClaim) {
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

    override fun list(): MutableList<AuditClaim> {
        val out = mutableListOf<AuditClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditClaimStore(): AuditClaimStore {
    return MemoryAuditClaimStore(mutableMapOf<String, AuditClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditClaimStore(
    var inner: AuditClaimStore,
    var cache: TtlCache<AuditClaim>,
    var hits: Int,
    var misses: Int,
) : AuditClaimStore {
    override fun find(code: String): AuditClaim? {
        val hit = (cache.get(code) as AuditClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditClaim> = inner.list()
}

fun newCachedAuditClaimStore(inner: AuditClaimStore, ttl: Int, clock: Clock): AuditClaimStore {
    return CachedAuditClaimStore(
        inner = inner,
        cache = newTtlCache<AuditClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit claims. Empty fields match all. */
data class AuditClaimQuery(
    var label: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditClaim): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AuditClaim>): MutableList<AuditClaim> {
        val matched = filter(items) { candidate: AuditClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditClaims(store: AuditClaimStore, query: AuditClaimQuery): MutableList<AuditClaim> {
    return query.apply(store.list())
}
