package com.acme.transport.lastmile.parcel.parcelbaseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps capped parcel quotas by code. */
interface CappedParcelQuotaStore {
    fun find(code: String): CappedParcelQuota?
    fun save(item: CappedParcelQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedParcelQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedParcelQuotaStore(
    var items: MutableMap<String, CappedParcelQuota>,
    var order: MutableList<String>,
) : CappedParcelQuotaStore {
    override fun find(code: String): CappedParcelQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedParcelQuota) {
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

    override fun list(): MutableList<CappedParcelQuota> {
        val out = mutableListOf<CappedParcelQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedParcelQuotaStore(): CappedParcelQuotaStore {
    return MemoryCappedParcelQuotaStore(mutableMapOf<String, CappedParcelQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedParcelQuotaStore(
    var inner: CappedParcelQuotaStore,
    var cache: TtlCache<CappedParcelQuota>,
    var hits: Int,
    var misses: Int,
) : CappedParcelQuotaStore {
    override fun find(code: String): CappedParcelQuota? {
        val hit = (cache.get(code) as CappedParcelQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedParcelQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedParcelQuota> = inner.list()
}

fun newCachedCappedParcelQuotaStore(inner: CappedParcelQuotaStore, ttl: Int, clock: Clock): CappedParcelQuotaStore {
    return CachedCappedParcelQuotaStore(
        inner = inner,
        cache = newTtlCache<CappedParcelQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped parcel quotas. Empty fields match all. */
data class CappedParcelQuotaQuery(
    var unit: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedParcelQuota): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedParcelQuota>): MutableList<CappedParcelQuota> {
        val matched = filter(items) { candidate: CappedParcelQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedParcelQuotas(store: CappedParcelQuotaStore, query: CappedParcelQuotaQuery): MutableList<CappedParcelQuota> {
    return query.apply(store.list())
}
