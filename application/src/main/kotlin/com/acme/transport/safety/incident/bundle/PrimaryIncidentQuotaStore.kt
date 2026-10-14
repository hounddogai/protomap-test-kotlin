package com.acme.transport.safety.incident.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary incident quotas by code. */
interface PrimaryIncidentQuotaStore {
    fun find(code: String): PrimaryIncidentQuota?
    fun save(item: PrimaryIncidentQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryIncidentQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryIncidentQuotaStore(
    var items: MutableMap<String, PrimaryIncidentQuota>,
    var order: MutableList<String>,
) : PrimaryIncidentQuotaStore {
    override fun find(code: String): PrimaryIncidentQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryIncidentQuota) {
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

    override fun list(): MutableList<PrimaryIncidentQuota> {
        val out = mutableListOf<PrimaryIncidentQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryIncidentQuotaStore(): PrimaryIncidentQuotaStore {
    return MemoryPrimaryIncidentQuotaStore(
        mutableMapOf<String, PrimaryIncidentQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryIncidentQuotaStore(
    var inner: PrimaryIncidentQuotaStore,
    var cache: TtlCache<PrimaryIncidentQuota>,
    var hits: Int,
    var misses: Int,
) : PrimaryIncidentQuotaStore {
    override fun find(code: String): PrimaryIncidentQuota? {
        val hit = (cache.get(code) as PrimaryIncidentQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryIncidentQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryIncidentQuota> = inner.list()
}

fun newCachedPrimaryIncidentQuotaStore(inner: PrimaryIncidentQuotaStore, ttl: Int, clock: Clock): PrimaryIncidentQuotaStore {
    return CachedPrimaryIncidentQuotaStore(
        inner = inner,
        cache = newTtlCache<PrimaryIncidentQuota>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary incident quotas. Empty fields match all. */
data class PrimaryIncidentQuotaQuery(
    var lane: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryIncidentQuota): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryIncidentQuota>): MutableList<PrimaryIncidentQuota> {
        val matched = filter(items) { candidate: PrimaryIncidentQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryIncidentQuotas(store: PrimaryIncidentQuotaStore, query: PrimaryIncidentQuotaQuery): MutableList<PrimaryIncidentQuota> {
    return query.apply(store.list())
}
