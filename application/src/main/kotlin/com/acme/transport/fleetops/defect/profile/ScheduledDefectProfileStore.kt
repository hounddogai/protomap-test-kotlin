package com.acme.transport.fleetops.defect.profile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled defect profiles by code. */
interface ScheduledDefectProfileStore {
    fun find(code: String): ScheduledDefectProfile?
    fun save(item: ScheduledDefectProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDefectProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDefectProfileStore(
    var items: MutableMap<String, ScheduledDefectProfile>,
    var order: MutableList<String>,
) : ScheduledDefectProfileStore {
    override fun find(code: String): ScheduledDefectProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDefectProfile) {
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

    override fun list(): MutableList<ScheduledDefectProfile> {
        val out = mutableListOf<ScheduledDefectProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDefectProfileStore(): ScheduledDefectProfileStore {
    return MemoryScheduledDefectProfileStore(
        mutableMapOf<String, ScheduledDefectProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDefectProfileStore(
    var inner: ScheduledDefectProfileStore,
    var cache: TtlCache<ScheduledDefectProfile>,
    var hits: Int,
    var misses: Int,
) : ScheduledDefectProfileStore {
    override fun find(code: String): ScheduledDefectProfile? {
        val hit = (cache.get(code) as ScheduledDefectProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDefectProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDefectProfile> = inner.list()
}

fun newCachedScheduledDefectProfileStore(inner: ScheduledDefectProfileStore, ttl: Int, clock: Clock): ScheduledDefectProfileStore {
    return CachedScheduledDefectProfileStore(
        inner = inner,
        cache = newTtlCache<ScheduledDefectProfile>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled defect profiles. Empty fields match all. */
data class ScheduledDefectProfileQuery(
    var unit: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDefectProfile): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<ScheduledDefectProfile>): MutableList<ScheduledDefectProfile> {
        val matched = filter(items) { candidate: ScheduledDefectProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDefectProfiles(store: ScheduledDefectProfileStore, query: ScheduledDefectProfileQuery): MutableList<ScheduledDefectProfile> {
    return query.apply(store.list())
}
