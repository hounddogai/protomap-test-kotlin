package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps manual attempt quotas by code. */
interface ManualAttemptQuotaStore {
    fun find(code: String): ManualAttemptQuota?
    fun save(item: ManualAttemptQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualAttemptQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualAttemptQuotaStore(
    var items: MutableMap<String, ManualAttemptQuota>,
    var order: MutableList<String>,
) : ManualAttemptQuotaStore {
    override fun find(code: String): ManualAttemptQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualAttemptQuota) {
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

    override fun list(): MutableList<ManualAttemptQuota> {
        val out = mutableListOf<ManualAttemptQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualAttemptQuotaStore(): ManualAttemptQuotaStore {
    return MemoryManualAttemptQuotaStore(mutableMapOf<String, ManualAttemptQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualAttemptQuotaStore(
    var inner: ManualAttemptQuotaStore,
    var cache: TtlCache<ManualAttemptQuota>,
    var hits: Int,
    var misses: Int,
) : ManualAttemptQuotaStore {
    override fun find(code: String): ManualAttemptQuota? {
        val hit = (cache.get(code) as ManualAttemptQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualAttemptQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualAttemptQuota> = inner.list()
}

fun newCachedManualAttemptQuotaStore(inner: ManualAttemptQuotaStore, ttl: Int, clock: Clock): ManualAttemptQuotaStore {
    return CachedManualAttemptQuotaStore(
        inner = inner,
        cache = newTtlCache<ManualAttemptQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual attempt quotas. Empty fields match all. */
data class ManualAttemptQuotaQuery(
    var segment: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualAttemptQuota): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualAttemptQuota>): MutableList<ManualAttemptQuota> {
        val matched = filter(items) { candidate: ManualAttemptQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualAttemptQuotas(store: ManualAttemptQuotaStore, query: ManualAttemptQuotaQuery): MutableList<ManualAttemptQuota> {
    return query.apply(store.list())
}
