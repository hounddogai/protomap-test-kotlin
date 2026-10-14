package com.acme.transport.crewing.qualification.rollup

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary qualification quotas by code. */
interface PrimaryQualificationQuotaStore {
    fun find(code: String): PrimaryQualificationQuota?
    fun save(item: PrimaryQualificationQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryQualificationQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryQualificationQuotaStore(
    var items: MutableMap<String, PrimaryQualificationQuota>,
    var order: MutableList<String>,
) : PrimaryQualificationQuotaStore {
    override fun find(code: String): PrimaryQualificationQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryQualificationQuota) {
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

    override fun list(): MutableList<PrimaryQualificationQuota> {
        val out = mutableListOf<PrimaryQualificationQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryQualificationQuotaStore(): PrimaryQualificationQuotaStore {
    return MemoryPrimaryQualificationQuotaStore(
        mutableMapOf<String, PrimaryQualificationQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryQualificationQuotaStore(
    var inner: PrimaryQualificationQuotaStore,
    var cache: TtlCache<PrimaryQualificationQuota>,
    var hits: Int,
    var misses: Int,
) : PrimaryQualificationQuotaStore {
    override fun find(code: String): PrimaryQualificationQuota? {
        val hit = (cache.get(code) as PrimaryQualificationQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryQualificationQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryQualificationQuota> = inner.list()
}

fun newCachedPrimaryQualificationQuotaStore(inner: PrimaryQualificationQuotaStore, ttl: Int, clock: Clock): PrimaryQualificationQuotaStore {
    return CachedPrimaryQualificationQuotaStore(
        inner = inner,
        cache = newTtlCache<PrimaryQualificationQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary qualification quotas. Empty fields match all. */
data class PrimaryQualificationQuotaQuery(
    var country: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryQualificationQuota): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<PrimaryQualificationQuota>): MutableList<PrimaryQualificationQuota> {
        val matched = filter(items) { candidate: PrimaryQualificationQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryQualificationQuotas(store: PrimaryQualificationQuotaStore, query: PrimaryQualificationQuotaQuery): MutableList<PrimaryQualificationQuota> {
    return query.apply(store.list())
}
