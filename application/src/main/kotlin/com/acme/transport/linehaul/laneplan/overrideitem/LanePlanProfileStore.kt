package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan profiles by code. */
interface LanePlanProfileStore {
    fun find(code: String): LanePlanProfile?
    fun save(item: LanePlanProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanProfileStore(
    var items: MutableMap<String, LanePlanProfile>,
    var order: MutableList<String>,
) : LanePlanProfileStore {
    override fun find(code: String): LanePlanProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanProfile) {
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

    override fun list(): MutableList<LanePlanProfile> {
        val out = mutableListOf<LanePlanProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanProfileStore(): LanePlanProfileStore {
    return MemoryLanePlanProfileStore(mutableMapOf<String, LanePlanProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanProfileStore(
    var inner: LanePlanProfileStore,
    var cache: TtlCache<LanePlanProfile>,
    var hits: Int,
    var misses: Int,
) : LanePlanProfileStore {
    override fun find(code: String): LanePlanProfile? {
        val hit = (cache.get(code) as LanePlanProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanProfile> = inner.list()
}

fun newCachedLanePlanProfileStore(inner: LanePlanProfileStore, ttl: Int, clock: Clock): LanePlanProfileStore {
    return CachedLanePlanProfileStore(
        inner = inner,
        cache = newTtlCache<LanePlanProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan profiles. Empty fields match all. */
data class LanePlanProfileQuery(
    var label: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanProfile): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<LanePlanProfile>): MutableList<LanePlanProfile> {
        val matched = filter(items) { candidate: LanePlanProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanProfiles(store: LanePlanProfileStore, query: LanePlanProfileQuery): MutableList<LanePlanProfile> {
    return query.apply(store.list())
}
