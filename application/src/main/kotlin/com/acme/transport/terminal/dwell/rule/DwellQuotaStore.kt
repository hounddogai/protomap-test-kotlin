package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell quotas by code. */
interface DwellQuotaStore {
    fun find(code: String): DwellQuota?
    fun save(item: DwellQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellQuotaStore(
    var items: MutableMap<String, DwellQuota>,
    var order: MutableList<String>,
) : DwellQuotaStore {
    override fun find(code: String): DwellQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellQuota) {
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

    override fun list(): MutableList<DwellQuota> {
        val out = mutableListOf<DwellQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellQuotaStore(): DwellQuotaStore {
    return MemoryDwellQuotaStore(mutableMapOf<String, DwellQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellQuotaStore(
    var inner: DwellQuotaStore,
    var cache: TtlCache<DwellQuota>,
    var hits: Int,
    var misses: Int,
) : DwellQuotaStore {
    override fun find(code: String): DwellQuota? {
        val hit = (cache.get(code) as DwellQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellQuota> = inner.list()
}

fun newCachedDwellQuotaStore(inner: DwellQuotaStore, ttl: Int, clock: Clock): DwellQuotaStore {
    return CachedDwellQuotaStore(
        inner = inner,
        cache = newTtlCache<DwellQuota>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell quotas. Empty fields match all. */
data class DwellQuotaQuery(
    var country: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellQuota): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellQuota>): MutableList<DwellQuota> {
        val matched = filter(items) { candidate: DwellQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellQuotas(store: DwellQuotaStore, query: DwellQuotaQuery): MutableList<DwellQuota> {
    return query.apply(store.list())
}
