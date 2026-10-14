package com.acme.transport.crewing.dutyperiod.snapshot

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period profiles by code. */
interface DutyPeriodProfileStore {
    fun find(code: String): DutyPeriodProfile?
    fun save(item: DutyPeriodProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodProfileStore(
    var items: MutableMap<String, DutyPeriodProfile>,
    var order: MutableList<String>,
) : DutyPeriodProfileStore {
    override fun find(code: String): DutyPeriodProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodProfile) {
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

    override fun list(): MutableList<DutyPeriodProfile> {
        val out = mutableListOf<DutyPeriodProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodProfileStore(): DutyPeriodProfileStore {
    return MemoryDutyPeriodProfileStore(mutableMapOf<String, DutyPeriodProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodProfileStore(
    var inner: DutyPeriodProfileStore,
    var cache: TtlCache<DutyPeriodProfile>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodProfileStore {
    override fun find(code: String): DutyPeriodProfile? {
        val hit = (cache.get(code) as DutyPeriodProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodProfile> = inner.list()
}

fun newCachedDutyPeriodProfileStore(inner: DutyPeriodProfileStore, ttl: Int, clock: Clock): DutyPeriodProfileStore {
    return CachedDutyPeriodProfileStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period profiles. Empty fields match all. */
data class DutyPeriodProfileQuery(
    var country: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodProfile): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodProfile>): MutableList<DutyPeriodProfile> {
        val matched = filter(items) { candidate: DutyPeriodProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodProfiles(store: DutyPeriodProfileStore, query: DutyPeriodProfileQuery): MutableList<DutyPeriodProfile> {
    return query.apply(store.list())
}
