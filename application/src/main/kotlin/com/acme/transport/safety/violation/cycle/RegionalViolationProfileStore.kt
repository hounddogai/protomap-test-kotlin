package com.acme.transport.safety.violation.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps regional violation profiles by code. */
interface RegionalViolationProfileStore {
    fun find(code: String): RegionalViolationProfile?
    fun save(item: RegionalViolationProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalViolationProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalViolationProfileStore(
    var items: MutableMap<String, RegionalViolationProfile>,
    var order: MutableList<String>,
) : RegionalViolationProfileStore {
    override fun find(code: String): RegionalViolationProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalViolationProfile) {
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

    override fun list(): MutableList<RegionalViolationProfile> {
        val out = mutableListOf<RegionalViolationProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalViolationProfileStore(): RegionalViolationProfileStore {
    return MemoryRegionalViolationProfileStore(
        mutableMapOf<String, RegionalViolationProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalViolationProfileStore(
    var inner: RegionalViolationProfileStore,
    var cache: TtlCache<RegionalViolationProfile>,
    var hits: Int,
    var misses: Int,
) : RegionalViolationProfileStore {
    override fun find(code: String): RegionalViolationProfile? {
        val hit = (cache.get(code) as RegionalViolationProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalViolationProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalViolationProfile> = inner.list()
}

fun newCachedRegionalViolationProfileStore(inner: RegionalViolationProfileStore, ttl: Int, clock: Clock): RegionalViolationProfileStore {
    return CachedRegionalViolationProfileStore(
        inner = inner,
        cache = newTtlCache<RegionalViolationProfile>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional violation profiles. Empty fields match all. */
data class RegionalViolationProfileQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalViolationProfile): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<RegionalViolationProfile>): MutableList<RegionalViolationProfile> {
        val matched = filter(items) { candidate: RegionalViolationProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalViolationProfiles(store: RegionalViolationProfileStore, query: RegionalViolationProfileQuery): MutableList<RegionalViolationProfile> {
    return query.apply(store.list())
}
