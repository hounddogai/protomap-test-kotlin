package com.acme.transport.crewing.dutyperiod.claim

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional duty period profiles by code. */
interface RegionalDutyPeriodProfileStore {
    fun find(code: String): RegionalDutyPeriodProfile?
    fun save(item: RegionalDutyPeriodProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDutyPeriodProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDutyPeriodProfileStore(
    var items: MutableMap<String, RegionalDutyPeriodProfile>,
    var order: MutableList<String>,
) : RegionalDutyPeriodProfileStore {
    override fun find(code: String): RegionalDutyPeriodProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDutyPeriodProfile) {
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

    override fun list(): MutableList<RegionalDutyPeriodProfile> {
        val out = mutableListOf<RegionalDutyPeriodProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDutyPeriodProfileStore(): RegionalDutyPeriodProfileStore {
    return MemoryRegionalDutyPeriodProfileStore(
        mutableMapOf<String, RegionalDutyPeriodProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDutyPeriodProfileStore(
    var inner: RegionalDutyPeriodProfileStore,
    var cache: TtlCache<RegionalDutyPeriodProfile>,
    var hits: Int,
    var misses: Int,
) : RegionalDutyPeriodProfileStore {
    override fun find(code: String): RegionalDutyPeriodProfile? {
        val hit = (cache.get(code) as RegionalDutyPeriodProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDutyPeriodProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDutyPeriodProfile> = inner.list()
}

fun newCachedRegionalDutyPeriodProfileStore(inner: RegionalDutyPeriodProfileStore, ttl: Int, clock: Clock): RegionalDutyPeriodProfileStore {
    return CachedRegionalDutyPeriodProfileStore(
        inner = inner,
        cache = newTtlCache<RegionalDutyPeriodProfile>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional duty period profiles. Empty fields match all. */
data class RegionalDutyPeriodProfileQuery(
    var channel: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDutyPeriodProfile): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalDutyPeriodProfile>): MutableList<RegionalDutyPeriodProfile> {
        val matched = filter(items) { candidate: RegionalDutyPeriodProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDutyPeriodProfiles(store: RegionalDutyPeriodProfileStore, query: RegionalDutyPeriodProfileQuery): MutableList<RegionalDutyPeriodProfile> {
    return query.apply(store.list())
}
