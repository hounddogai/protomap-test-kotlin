package com.acme.transport.crewing.roster.quota

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster quotas by code. */
interface RosterQuotaStore {
    fun find(code: String): RosterQuota?
    fun save(item: RosterQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterQuotaStore(
    var items: MutableMap<String, RosterQuota>,
    var order: MutableList<String>,
) : RosterQuotaStore {
    override fun find(code: String): RosterQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterQuota) {
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

    override fun list(): MutableList<RosterQuota> {
        val out = mutableListOf<RosterQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterQuotaStore(): RosterQuotaStore {
    return MemoryRosterQuotaStore(mutableMapOf<String, RosterQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterQuotaStore(
    var inner: RosterQuotaStore,
    var cache: TtlCache<RosterQuota>,
    var hits: Int,
    var misses: Int,
) : RosterQuotaStore {
    override fun find(code: String): RosterQuota? {
        val hit = (cache.get(code) as RosterQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterQuota> = inner.list()
}

fun newCachedRosterQuotaStore(inner: RosterQuotaStore, ttl: Int, clock: Clock): RosterQuotaStore {
    return CachedRosterQuotaStore(
        inner = inner,
        cache = newTtlCache<RosterQuota>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster quotas. Empty fields match all. */
data class RosterQuotaQuery(
    var title: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterQuota): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterQuota>): MutableList<RosterQuota> {
        val matched = filter(items) { candidate: RosterQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterQuotas(store: RosterQuotaStore, query: RosterQuotaQuery): MutableList<RosterQuota> {
    return query.apply(store.list())
}
