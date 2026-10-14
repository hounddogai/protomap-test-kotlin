package com.acme.transport.freightpay.audit.notice

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit quotas by code. */
interface AuditQuotaStore {
    fun find(code: String): AuditQuota?
    fun save(item: AuditQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditQuotaStore(
    var items: MutableMap<String, AuditQuota>,
    var order: MutableList<String>,
) : AuditQuotaStore {
    override fun find(code: String): AuditQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditQuota) {
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

    override fun list(): MutableList<AuditQuota> {
        val out = mutableListOf<AuditQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditQuotaStore(): AuditQuotaStore {
    return MemoryAuditQuotaStore(mutableMapOf<String, AuditQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditQuotaStore(
    var inner: AuditQuotaStore,
    var cache: TtlCache<AuditQuota>,
    var hits: Int,
    var misses: Int,
) : AuditQuotaStore {
    override fun find(code: String): AuditQuota? {
        val hit = (cache.get(code) as AuditQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditQuota> = inner.list()
}

fun newCachedAuditQuotaStore(inner: AuditQuotaStore, ttl: Int, clock: Clock): AuditQuotaStore {
    return CachedAuditQuotaStore(
        inner = inner,
        cache = newTtlCache<AuditQuota>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit quotas. Empty fields match all. */
data class AuditQuotaQuery(
    var channel: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditQuota): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AuditQuota>): MutableList<AuditQuota> {
        val matched = filter(items) { candidate: AuditQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditQuotas(store: AuditQuotaStore, query: AuditQuotaQuery): MutableList<AuditQuota> {
    return query.apply(store.list())
}
