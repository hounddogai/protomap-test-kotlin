package com.acme.transport.network.zone.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone quotas by code. */
interface ZoneQuotaStore {
    fun find(code: String): ZoneQuota?
    fun save(item: ZoneQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneQuotaStore(
    var items: MutableMap<String, ZoneQuota>,
    var order: MutableList<String>,
) : ZoneQuotaStore {
    override fun find(code: String): ZoneQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneQuota) {
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

    override fun list(): MutableList<ZoneQuota> {
        val out = mutableListOf<ZoneQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneQuotaStore(): ZoneQuotaStore {
    return MemoryZoneQuotaStore(mutableMapOf<String, ZoneQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneQuotaStore(
    var inner: ZoneQuotaStore,
    var cache: TtlCache<ZoneQuota>,
    var hits: Int,
    var misses: Int,
) : ZoneQuotaStore {
    override fun find(code: String): ZoneQuota? {
        val hit = (cache.get(code) as ZoneQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneQuota> = inner.list()
}

fun newCachedZoneQuotaStore(inner: ZoneQuotaStore, ttl: Int, clock: Clock): ZoneQuotaStore {
    return CachedZoneQuotaStore(
        inner = inner,
        cache = newTtlCache<ZoneQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone quotas. Empty fields match all. */
data class ZoneQuotaQuery(
    var lane: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneQuota): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ZoneQuota>): MutableList<ZoneQuota> {
        val matched = filter(items) { candidate: ZoneQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneQuotas(store: ZoneQuotaStore, query: ZoneQuotaQuery): MutableList<ZoneQuota> {
    return query.apply(store.list())
}
