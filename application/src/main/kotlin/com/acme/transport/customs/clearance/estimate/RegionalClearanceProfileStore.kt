package com.acme.transport.customs.clearance.estimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional clearance profiles by code. */
interface RegionalClearanceProfileStore {
    fun find(code: String): RegionalClearanceProfile?
    fun save(item: RegionalClearanceProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalClearanceProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalClearanceProfileStore(
    var items: MutableMap<String, RegionalClearanceProfile>,
    var order: MutableList<String>,
) : RegionalClearanceProfileStore {
    override fun find(code: String): RegionalClearanceProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalClearanceProfile) {
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

    override fun list(): MutableList<RegionalClearanceProfile> {
        val out = mutableListOf<RegionalClearanceProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalClearanceProfileStore(): RegionalClearanceProfileStore {
    return MemoryRegionalClearanceProfileStore(
        mutableMapOf<String, RegionalClearanceProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalClearanceProfileStore(
    var inner: RegionalClearanceProfileStore,
    var cache: TtlCache<RegionalClearanceProfile>,
    var hits: Int,
    var misses: Int,
) : RegionalClearanceProfileStore {
    override fun find(code: String): RegionalClearanceProfile? {
        val hit = (cache.get(code) as RegionalClearanceProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalClearanceProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalClearanceProfile> = inner.list()
}

fun newCachedRegionalClearanceProfileStore(inner: RegionalClearanceProfileStore, ttl: Int, clock: Clock): RegionalClearanceProfileStore {
    return CachedRegionalClearanceProfileStore(
        inner = inner,
        cache = newTtlCache<RegionalClearanceProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional clearance profiles. Empty fields match all. */
data class RegionalClearanceProfileQuery(
    var country: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalClearanceProfile): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalClearanceProfile>): MutableList<RegionalClearanceProfile> {
        val matched = filter(items) { candidate: RegionalClearanceProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalClearanceProfiles(store: RegionalClearanceProfileStore, query: RegionalClearanceProfileQuery): MutableList<RegionalClearanceProfile> {
    return query.apply(store.list())
}
