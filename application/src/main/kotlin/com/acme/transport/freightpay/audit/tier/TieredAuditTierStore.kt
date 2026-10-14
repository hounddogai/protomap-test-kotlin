package com.acme.transport.freightpay.audit.tier

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered audit tiers by code. */
interface TieredAuditTierStore {
    fun find(code: String): TieredAuditTier?
    fun save(item: TieredAuditTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredAuditTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredAuditTierStore(
    var items: MutableMap<String, TieredAuditTier>,
    var order: MutableList<String>,
) : TieredAuditTierStore {
    override fun find(code: String): TieredAuditTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredAuditTier) {
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

    override fun list(): MutableList<TieredAuditTier> {
        val out = mutableListOf<TieredAuditTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredAuditTierStore(): TieredAuditTierStore {
    return MemoryTieredAuditTierStore(mutableMapOf<String, TieredAuditTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredAuditTierStore(
    var inner: TieredAuditTierStore,
    var cache: TtlCache<TieredAuditTier>,
    var hits: Int,
    var misses: Int,
) : TieredAuditTierStore {
    override fun find(code: String): TieredAuditTier? {
        val hit = (cache.get(code) as TieredAuditTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredAuditTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredAuditTier> = inner.list()
}

fun newCachedTieredAuditTierStore(inner: TieredAuditTierStore, ttl: Int, clock: Clock): TieredAuditTierStore {
    return CachedTieredAuditTierStore(
        inner = inner,
        cache = newTtlCache<TieredAuditTier>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered audit tiers. Empty fields match all. */
data class TieredAuditTierQuery(
    var segment: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredAuditTier): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<TieredAuditTier>): MutableList<TieredAuditTier> {
        val matched = filter(items) { candidate: TieredAuditTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredAuditTiers(store: TieredAuditTierStore, query: TieredAuditTierQuery): MutableList<TieredAuditTier> {
    return query.apply(store.list())
}
