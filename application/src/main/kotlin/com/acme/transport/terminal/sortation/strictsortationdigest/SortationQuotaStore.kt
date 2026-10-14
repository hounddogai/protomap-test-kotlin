package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation quotas by code. */
interface SortationQuotaStore {
    fun find(code: String): SortationQuota?
    fun save(item: SortationQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationQuotaStore(
    var items: MutableMap<String, SortationQuota>,
    var order: MutableList<String>,
) : SortationQuotaStore {
    override fun find(code: String): SortationQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationQuota) {
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

    override fun list(): MutableList<SortationQuota> {
        val out = mutableListOf<SortationQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationQuotaStore(): SortationQuotaStore {
    return MemorySortationQuotaStore(mutableMapOf<String, SortationQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationQuotaStore(
    var inner: SortationQuotaStore,
    var cache: TtlCache<SortationQuota>,
    var hits: Int,
    var misses: Int,
) : SortationQuotaStore {
    override fun find(code: String): SortationQuota? {
        val hit = (cache.get(code) as SortationQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationQuota> = inner.list()
}

fun newCachedSortationQuotaStore(inner: SortationQuotaStore, ttl: Int, clock: Clock): SortationQuotaStore {
    return CachedSortationQuotaStore(
        inner = inner,
        cache = newTtlCache<SortationQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation quotas. Empty fields match all. */
data class SortationQuotaQuery(
    var category: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationQuota): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<SortationQuota>): MutableList<SortationQuota> {
        val matched = filter(items) { candidate: SortationQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationQuotas(store: SortationQuotaStore, query: SortationQuotaQuery): MutableList<SortationQuota> {
    return query.apply(store.list())
}
