package com.acme.transport.terminal.traileryard.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps global trailer yard quotas by code. */
interface GlobalTrailerYardQuotaStore {
    fun find(code: String): GlobalTrailerYardQuota?
    fun save(item: GlobalTrailerYardQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalTrailerYardQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalTrailerYardQuotaStore(
    var items: MutableMap<String, GlobalTrailerYardQuota>,
    var order: MutableList<String>,
) : GlobalTrailerYardQuotaStore {
    override fun find(code: String): GlobalTrailerYardQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalTrailerYardQuota) {
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

    override fun list(): MutableList<GlobalTrailerYardQuota> {
        val out = mutableListOf<GlobalTrailerYardQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalTrailerYardQuotaStore(): GlobalTrailerYardQuotaStore {
    return MemoryGlobalTrailerYardQuotaStore(
        mutableMapOf<String, GlobalTrailerYardQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalTrailerYardQuotaStore(
    var inner: GlobalTrailerYardQuotaStore,
    var cache: TtlCache<GlobalTrailerYardQuota>,
    var hits: Int,
    var misses: Int,
) : GlobalTrailerYardQuotaStore {
    override fun find(code: String): GlobalTrailerYardQuota? {
        val hit = (cache.get(code) as GlobalTrailerYardQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalTrailerYardQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalTrailerYardQuota> = inner.list()
}

fun newCachedGlobalTrailerYardQuotaStore(inner: GlobalTrailerYardQuotaStore, ttl: Int, clock: Clock): GlobalTrailerYardQuotaStore {
    return CachedGlobalTrailerYardQuotaStore(
        inner = inner,
        cache = newTtlCache<GlobalTrailerYardQuota>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global trailer yard quotas. Empty fields match all. */
data class GlobalTrailerYardQuotaQuery(
    var region: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalTrailerYardQuota): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<GlobalTrailerYardQuota>): MutableList<GlobalTrailerYardQuota> {
        val matched = filter(items) { candidate: GlobalTrailerYardQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalTrailerYardQuotas(store: GlobalTrailerYardQuotaStore, query: GlobalTrailerYardQuotaQuery): MutableList<GlobalTrailerYardQuota> {
    return query.apply(store.list())
}
