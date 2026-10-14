package com.acme.transport.freightpay.audit.lenientauditproposal

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit tiers by code. */
interface AuditTierStore {
    fun find(code: String): AuditTier?
    fun save(item: AuditTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditTierStore(
    var items: MutableMap<String, AuditTier>,
    var order: MutableList<String>,
) : AuditTierStore {
    override fun find(code: String): AuditTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditTier) {
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

    override fun list(): MutableList<AuditTier> {
        val out = mutableListOf<AuditTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditTierStore(): AuditTierStore {
    return MemoryAuditTierStore(mutableMapOf<String, AuditTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditTierStore(
    var inner: AuditTierStore,
    var cache: TtlCache<AuditTier>,
    var hits: Int,
    var misses: Int,
) : AuditTierStore {
    override fun find(code: String): AuditTier? {
        val hit = (cache.get(code) as AuditTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditTier> = inner.list()
}

fun newCachedAuditTierStore(inner: AuditTierStore, ttl: Int, clock: Clock): AuditTierStore {
    return CachedAuditTierStore(
        inner = inner,
        cache = newTtlCache<AuditTier>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit tiers. Empty fields match all. */
data class AuditTierQuery(
    var lane: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditTier): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AuditTier>): MutableList<AuditTier> {
        val matched = filter(items) { candidate: AuditTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditTiers(store: AuditTierStore, query: AuditTierQuery): MutableList<AuditTier> {
    return query.apply(store.list())
}
