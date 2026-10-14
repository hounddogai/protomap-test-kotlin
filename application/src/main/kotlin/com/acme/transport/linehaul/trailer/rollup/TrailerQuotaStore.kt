package com.acme.transport.linehaul.trailer.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer quotas by code. */
interface TrailerQuotaStore {
    fun find(code: String): TrailerQuota?
    fun save(item: TrailerQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerQuotaStore(
    var items: MutableMap<String, TrailerQuota>,
    var order: MutableList<String>,
) : TrailerQuotaStore {
    override fun find(code: String): TrailerQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerQuota) {
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

    override fun list(): MutableList<TrailerQuota> {
        val out = mutableListOf<TrailerQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerQuotaStore(): TrailerQuotaStore {
    return MemoryTrailerQuotaStore(mutableMapOf<String, TrailerQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerQuotaStore(
    var inner: TrailerQuotaStore,
    var cache: TtlCache<TrailerQuota>,
    var hits: Int,
    var misses: Int,
) : TrailerQuotaStore {
    override fun find(code: String): TrailerQuota? {
        val hit = (cache.get(code) as TrailerQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerQuota> = inner.list()
}

fun newCachedTrailerQuotaStore(inner: TrailerQuotaStore, ttl: Int, clock: Clock): TrailerQuotaStore {
    return CachedTrailerQuotaStore(
        inner = inner,
        cache = newTtlCache<TrailerQuota>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer quotas. Empty fields match all. */
data class TrailerQuotaQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerQuota): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<TrailerQuota>): MutableList<TrailerQuota> {
        val matched = filter(items) { candidate: TrailerQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerQuotas(store: TrailerQuotaStore, query: TrailerQuotaQuery): MutableList<TrailerQuota> {
    return query.apply(store.list())
}
