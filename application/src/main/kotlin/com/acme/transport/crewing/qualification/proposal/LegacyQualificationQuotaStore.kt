package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy qualification quotas by code. */
interface LegacyQualificationQuotaStore {
    fun find(code: String): LegacyQualificationQuota?
    fun save(item: LegacyQualificationQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyQualificationQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyQualificationQuotaStore(
    var items: MutableMap<String, LegacyQualificationQuota>,
    var order: MutableList<String>,
) : LegacyQualificationQuotaStore {
    override fun find(code: String): LegacyQualificationQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyQualificationQuota) {
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

    override fun list(): MutableList<LegacyQualificationQuota> {
        val out = mutableListOf<LegacyQualificationQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyQualificationQuotaStore(): LegacyQualificationQuotaStore {
    return MemoryLegacyQualificationQuotaStore(
        mutableMapOf<String, LegacyQualificationQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyQualificationQuotaStore(
    var inner: LegacyQualificationQuotaStore,
    var cache: TtlCache<LegacyQualificationQuota>,
    var hits: Int,
    var misses: Int,
) : LegacyQualificationQuotaStore {
    override fun find(code: String): LegacyQualificationQuota? {
        val hit = (cache.get(code) as LegacyQualificationQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyQualificationQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyQualificationQuota> = inner.list()
}

fun newCachedLegacyQualificationQuotaStore(inner: LegacyQualificationQuotaStore, ttl: Int, clock: Clock): LegacyQualificationQuotaStore {
    return CachedLegacyQualificationQuotaStore(
        inner = inner,
        cache = newTtlCache<LegacyQualificationQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy qualification quotas. Empty fields match all. */
data class LegacyQualificationQuotaQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyQualificationQuota): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyQualificationQuota>): MutableList<LegacyQualificationQuota> {
        val matched = filter(items) { candidate: LegacyQualificationQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyQualificationQuotas(store: LegacyQualificationQuotaStore, query: LegacyQualificationQuotaQuery): MutableList<LegacyQualificationQuota> {
    return query.apply(store.list())
}
