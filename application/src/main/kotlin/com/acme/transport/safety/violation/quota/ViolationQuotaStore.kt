package com.acme.transport.safety.violation.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation quotas by code. */
interface ViolationQuotaStore {
    fun find(code: String): ViolationQuota?
    fun save(item: ViolationQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationQuotaStore(
    var items: MutableMap<String, ViolationQuota>,
    var order: MutableList<String>,
) : ViolationQuotaStore {
    override fun find(code: String): ViolationQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationQuota) {
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

    override fun list(): MutableList<ViolationQuota> {
        val out = mutableListOf<ViolationQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationQuotaStore(): ViolationQuotaStore {
    return MemoryViolationQuotaStore(mutableMapOf<String, ViolationQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationQuotaStore(
    var inner: ViolationQuotaStore,
    var cache: TtlCache<ViolationQuota>,
    var hits: Int,
    var misses: Int,
) : ViolationQuotaStore {
    override fun find(code: String): ViolationQuota? {
        val hit = (cache.get(code) as ViolationQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationQuota> = inner.list()
}

fun newCachedViolationQuotaStore(inner: ViolationQuotaStore, ttl: Int, clock: Clock): ViolationQuotaStore {
    return CachedViolationQuotaStore(
        inner = inner,
        cache = newTtlCache<ViolationQuota>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation quotas. Empty fields match all. */
data class ViolationQuotaQuery(
    var segment: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationQuota): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationQuota>): MutableList<ViolationQuota> {
        val matched = filter(items) { candidate: ViolationQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationQuotas(store: ViolationQuotaStore, query: ViolationQuotaQuery): MutableList<ViolationQuota> {
    return query.apply(store.list())
}
