package com.acme.transport.safety.violation.manualviolationquota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict violation quotas by code. */
interface StrictViolationQuotaStore {
    fun find(code: String): StrictViolationQuota?
    fun save(item: StrictViolationQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictViolationQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictViolationQuotaStore(
    var items: MutableMap<String, StrictViolationQuota>,
    var order: MutableList<String>,
) : StrictViolationQuotaStore {
    override fun find(code: String): StrictViolationQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictViolationQuota) {
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

    override fun list(): MutableList<StrictViolationQuota> {
        val out = mutableListOf<StrictViolationQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictViolationQuotaStore(): StrictViolationQuotaStore {
    return MemoryStrictViolationQuotaStore(
        mutableMapOf<String, StrictViolationQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictViolationQuotaStore(
    var inner: StrictViolationQuotaStore,
    var cache: TtlCache<StrictViolationQuota>,
    var hits: Int,
    var misses: Int,
) : StrictViolationQuotaStore {
    override fun find(code: String): StrictViolationQuota? {
        val hit = (cache.get(code) as StrictViolationQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictViolationQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictViolationQuota> = inner.list()
}

fun newCachedStrictViolationQuotaStore(inner: StrictViolationQuotaStore, ttl: Int, clock: Clock): StrictViolationQuotaStore {
    return CachedStrictViolationQuotaStore(
        inner = inner,
        cache = newTtlCache<StrictViolationQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict violation quotas. Empty fields match all. */
data class StrictViolationQuotaQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictViolationQuota): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictViolationQuota>): MutableList<StrictViolationQuota> {
        val matched = filter(items) { candidate: StrictViolationQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictViolationQuotas(store: StrictViolationQuotaStore, query: StrictViolationQuotaQuery): MutableList<StrictViolationQuota> {
    return query.apply(store.list())
}
