package com.acme.transport.safety.violation.primaryviolationquota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps lenient violation quotas by code. */
interface LenientViolationQuotaStore {
    fun find(code: String): LenientViolationQuota?
    fun save(item: LenientViolationQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientViolationQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientViolationQuotaStore(
    var items: MutableMap<String, LenientViolationQuota>,
    var order: MutableList<String>,
) : LenientViolationQuotaStore {
    override fun find(code: String): LenientViolationQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientViolationQuota) {
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

    override fun list(): MutableList<LenientViolationQuota> {
        val out = mutableListOf<LenientViolationQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientViolationQuotaStore(): LenientViolationQuotaStore {
    return MemoryLenientViolationQuotaStore(
        mutableMapOf<String, LenientViolationQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientViolationQuotaStore(
    var inner: LenientViolationQuotaStore,
    var cache: TtlCache<LenientViolationQuota>,
    var hits: Int,
    var misses: Int,
) : LenientViolationQuotaStore {
    override fun find(code: String): LenientViolationQuota? {
        val hit = (cache.get(code) as LenientViolationQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientViolationQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientViolationQuota> = inner.list()
}

fun newCachedLenientViolationQuotaStore(inner: LenientViolationQuotaStore, ttl: Int, clock: Clock): LenientViolationQuotaStore {
    return CachedLenientViolationQuotaStore(
        inner = inner,
        cache = newTtlCache<LenientViolationQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient violation quotas. Empty fields match all. */
data class LenientViolationQuotaQuery(
    var depot: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientViolationQuota): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<LenientViolationQuota>): MutableList<LenientViolationQuota> {
        val matched = filter(items) { candidate: LenientViolationQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientViolationQuotas(store: LenientViolationQuotaStore, query: LenientViolationQuotaQuery): MutableList<LenientViolationQuota> {
    return query.apply(store.list())
}
