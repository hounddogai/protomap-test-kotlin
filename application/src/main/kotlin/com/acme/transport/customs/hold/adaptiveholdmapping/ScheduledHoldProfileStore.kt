package com.acme.transport.customs.hold.adaptiveholdmapping

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled hold profiles by code. */
interface ScheduledHoldProfileStore {
    fun find(code: String): ScheduledHoldProfile?
    fun save(item: ScheduledHoldProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledHoldProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledHoldProfileStore(
    var items: MutableMap<String, ScheduledHoldProfile>,
    var order: MutableList<String>,
) : ScheduledHoldProfileStore {
    override fun find(code: String): ScheduledHoldProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledHoldProfile) {
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

    override fun list(): MutableList<ScheduledHoldProfile> {
        val out = mutableListOf<ScheduledHoldProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledHoldProfileStore(): ScheduledHoldProfileStore {
    return MemoryScheduledHoldProfileStore(
        mutableMapOf<String, ScheduledHoldProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledHoldProfileStore(
    var inner: ScheduledHoldProfileStore,
    var cache: TtlCache<ScheduledHoldProfile>,
    var hits: Int,
    var misses: Int,
) : ScheduledHoldProfileStore {
    override fun find(code: String): ScheduledHoldProfile? {
        val hit = (cache.get(code) as ScheduledHoldProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledHoldProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledHoldProfile> = inner.list()
}

fun newCachedScheduledHoldProfileStore(inner: ScheduledHoldProfileStore, ttl: Int, clock: Clock): ScheduledHoldProfileStore {
    return CachedScheduledHoldProfileStore(
        inner = inner,
        cache = newTtlCache<ScheduledHoldProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled hold profiles. Empty fields match all. */
data class ScheduledHoldProfileQuery(
    var region: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledHoldProfile): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<ScheduledHoldProfile>): MutableList<ScheduledHoldProfile> {
        val matched = filter(items) { candidate: ScheduledHoldProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledHoldProfiles(store: ScheduledHoldProfileStore, query: ScheduledHoldProfileQuery): MutableList<ScheduledHoldProfile> {
    return query.apply(store.list())
}
