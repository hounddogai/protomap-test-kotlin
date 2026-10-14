package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel quotas by code. */
interface ParcelQuotaStore {
    fun find(code: String): ParcelQuota?
    fun save(item: ParcelQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelQuotaStore(
    var items: MutableMap<String, ParcelQuota>,
    var order: MutableList<String>,
) : ParcelQuotaStore {
    override fun find(code: String): ParcelQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelQuota) {
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

    override fun list(): MutableList<ParcelQuota> {
        val out = mutableListOf<ParcelQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelQuotaStore(): ParcelQuotaStore {
    return MemoryParcelQuotaStore(mutableMapOf<String, ParcelQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelQuotaStore(
    var inner: ParcelQuotaStore,
    var cache: TtlCache<ParcelQuota>,
    var hits: Int,
    var misses: Int,
) : ParcelQuotaStore {
    override fun find(code: String): ParcelQuota? {
        val hit = (cache.get(code) as ParcelQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelQuota> = inner.list()
}

fun newCachedParcelQuotaStore(inner: ParcelQuotaStore, ttl: Int, clock: Clock): ParcelQuotaStore {
    return CachedParcelQuotaStore(
        inner = inner,
        cache = newTtlCache<ParcelQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel quotas. Empty fields match all. */
data class ParcelQuotaQuery(
    var title: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelQuota): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelQuota>): MutableList<ParcelQuota> {
        val matched = filter(items) { candidate: ParcelQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelQuotas(store: ParcelQuotaStore, query: ParcelQuotaQuery): MutableList<ParcelQuota> {
    return query.apply(store.list())
}
