package com.acme.transport.lastmile.drop.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled drop quotas by code. */
interface ScheduledDropQuotaStore {
    fun find(code: String): ScheduledDropQuota?
    fun save(item: ScheduledDropQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDropQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDropQuotaStore(
    var items: MutableMap<String, ScheduledDropQuota>,
    var order: MutableList<String>,
) : ScheduledDropQuotaStore {
    override fun find(code: String): ScheduledDropQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDropQuota) {
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

    override fun list(): MutableList<ScheduledDropQuota> {
        val out = mutableListOf<ScheduledDropQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDropQuotaStore(): ScheduledDropQuotaStore {
    return MemoryScheduledDropQuotaStore(mutableMapOf<String, ScheduledDropQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDropQuotaStore(
    var inner: ScheduledDropQuotaStore,
    var cache: TtlCache<ScheduledDropQuota>,
    var hits: Int,
    var misses: Int,
) : ScheduledDropQuotaStore {
    override fun find(code: String): ScheduledDropQuota? {
        val hit = (cache.get(code) as ScheduledDropQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDropQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDropQuota> = inner.list()
}

fun newCachedScheduledDropQuotaStore(inner: ScheduledDropQuotaStore, ttl: Int, clock: Clock): ScheduledDropQuotaStore {
    return CachedScheduledDropQuotaStore(
        inner = inner,
        cache = newTtlCache<ScheduledDropQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled drop quotas. Empty fields match all. */
data class ScheduledDropQuotaQuery(
    var region: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDropQuota): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledDropQuota>): MutableList<ScheduledDropQuota> {
        val matched = filter(items) { candidate: ScheduledDropQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDropQuotas(store: ScheduledDropQuotaStore, query: ScheduledDropQuotaQuery): MutableList<ScheduledDropQuota> {
    return query.apply(store.list())
}
