package com.acme.transport.terminal.crossdock.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps lenient cross dock quotas by code. */
interface LenientCrossDockQuotaStore {
    fun find(code: String): LenientCrossDockQuota?
    fun save(item: LenientCrossDockQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientCrossDockQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientCrossDockQuotaStore(
    var items: MutableMap<String, LenientCrossDockQuota>,
    var order: MutableList<String>,
) : LenientCrossDockQuotaStore {
    override fun find(code: String): LenientCrossDockQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientCrossDockQuota) {
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

    override fun list(): MutableList<LenientCrossDockQuota> {
        val out = mutableListOf<LenientCrossDockQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientCrossDockQuotaStore(): LenientCrossDockQuotaStore {
    return MemoryLenientCrossDockQuotaStore(
        mutableMapOf<String, LenientCrossDockQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientCrossDockQuotaStore(
    var inner: LenientCrossDockQuotaStore,
    var cache: TtlCache<LenientCrossDockQuota>,
    var hits: Int,
    var misses: Int,
) : LenientCrossDockQuotaStore {
    override fun find(code: String): LenientCrossDockQuota? {
        val hit = (cache.get(code) as LenientCrossDockQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientCrossDockQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientCrossDockQuota> = inner.list()
}

fun newCachedLenientCrossDockQuotaStore(inner: LenientCrossDockQuotaStore, ttl: Int, clock: Clock): LenientCrossDockQuotaStore {
    return CachedLenientCrossDockQuotaStore(
        inner = inner,
        cache = newTtlCache<LenientCrossDockQuota>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient cross dock quotas. Empty fields match all. */
data class LenientCrossDockQuotaQuery(
    var region: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientCrossDockQuota): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientCrossDockQuota>): MutableList<LenientCrossDockQuota> {
        val matched = filter(items) { candidate: LenientCrossDockQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientCrossDockQuotas(store: LenientCrossDockQuotaStore, query: LenientCrossDockQuotaQuery): MutableList<LenientCrossDockQuota> {
    return query.apply(store.list())
}
