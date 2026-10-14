package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard quotas by code. */
interface TrailerYardQuotaStore {
    fun find(code: String): TrailerYardQuota?
    fun save(item: TrailerYardQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardQuotaStore(
    var items: MutableMap<String, TrailerYardQuota>,
    var order: MutableList<String>,
) : TrailerYardQuotaStore {
    override fun find(code: String): TrailerYardQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardQuota) {
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

    override fun list(): MutableList<TrailerYardQuota> {
        val out = mutableListOf<TrailerYardQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardQuotaStore(): TrailerYardQuotaStore {
    return MemoryTrailerYardQuotaStore(mutableMapOf<String, TrailerYardQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardQuotaStore(
    var inner: TrailerYardQuotaStore,
    var cache: TtlCache<TrailerYardQuota>,
    var hits: Int,
    var misses: Int,
) : TrailerYardQuotaStore {
    override fun find(code: String): TrailerYardQuota? {
        val hit = (cache.get(code) as TrailerYardQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardQuota> = inner.list()
}

fun newCachedTrailerYardQuotaStore(inner: TrailerYardQuotaStore, ttl: Int, clock: Clock): TrailerYardQuotaStore {
    return CachedTrailerYardQuotaStore(
        inner = inner,
        cache = newTtlCache<TrailerYardQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard quotas. Empty fields match all. */
data class TrailerYardQuotaQuery(
    var depot: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardQuota): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardQuota>): MutableList<TrailerYardQuota> {
        val matched = filter(items) { candidate: TrailerYardQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardQuotas(store: TrailerYardQuotaStore, query: TrailerYardQuotaQuery): MutableList<TrailerYardQuota> {
    return query.apply(store.list())
}
