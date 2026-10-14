package com.acme.transport.customs.hold.profile

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy hold profiles by code. */
interface LegacyHoldProfileStore {
    fun find(code: String): LegacyHoldProfile?
    fun save(item: LegacyHoldProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyHoldProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyHoldProfileStore(
    var items: MutableMap<String, LegacyHoldProfile>,
    var order: MutableList<String>,
) : LegacyHoldProfileStore {
    override fun find(code: String): LegacyHoldProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyHoldProfile) {
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

    override fun list(): MutableList<LegacyHoldProfile> {
        val out = mutableListOf<LegacyHoldProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyHoldProfileStore(): LegacyHoldProfileStore {
    return MemoryLegacyHoldProfileStore(mutableMapOf<String, LegacyHoldProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyHoldProfileStore(
    var inner: LegacyHoldProfileStore,
    var cache: TtlCache<LegacyHoldProfile>,
    var hits: Int,
    var misses: Int,
) : LegacyHoldProfileStore {
    override fun find(code: String): LegacyHoldProfile? {
        val hit = (cache.get(code) as LegacyHoldProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyHoldProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyHoldProfile> = inner.list()
}

fun newCachedLegacyHoldProfileStore(inner: LegacyHoldProfileStore, ttl: Int, clock: Clock): LegacyHoldProfileStore {
    return CachedLegacyHoldProfileStore(
        inner = inner,
        cache = newTtlCache<LegacyHoldProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy hold profiles. Empty fields match all. */
data class LegacyHoldProfileQuery(
    var unit: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyHoldProfile): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyHoldProfile>): MutableList<LegacyHoldProfile> {
        val matched = filter(items) { candidate: LegacyHoldProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyHoldProfiles(store: LegacyHoldProfileStore, query: LegacyHoldProfileQuery): MutableList<LegacyHoldProfile> {
    return query.apply(store.list())
}
