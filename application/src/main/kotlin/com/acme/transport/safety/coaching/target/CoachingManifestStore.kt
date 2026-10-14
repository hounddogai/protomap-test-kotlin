package com.acme.transport.safety.coaching.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching manifests by code. */
interface CoachingManifestStore {
    fun find(code: String): CoachingManifest?
    fun save(item: CoachingManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingManifestStore(
    var items: MutableMap<String, CoachingManifest>,
    var order: MutableList<String>,
) : CoachingManifestStore {
    override fun find(code: String): CoachingManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingManifest) {
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

    override fun list(): MutableList<CoachingManifest> {
        val out = mutableListOf<CoachingManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingManifestStore(): CoachingManifestStore {
    return MemoryCoachingManifestStore(mutableMapOf<String, CoachingManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingManifestStore(
    var inner: CoachingManifestStore,
    var cache: TtlCache<CoachingManifest>,
    var hits: Int,
    var misses: Int,
) : CoachingManifestStore {
    override fun find(code: String): CoachingManifest? {
        val hit = (cache.get(code) as CoachingManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingManifest> = inner.list()
}

fun newCachedCoachingManifestStore(inner: CoachingManifestStore, ttl: Int, clock: Clock): CoachingManifestStore {
    return CachedCoachingManifestStore(
        inner = inner,
        cache = newTtlCache<CoachingManifest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching manifests. Empty fields match all. */
data class CoachingManifestQuery(
    var segment: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingManifest): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingManifest>): MutableList<CoachingManifest> {
        val matched = filter(items) { candidate: CoachingManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingManifests(store: CoachingManifestStore, query: CoachingManifestQuery): MutableList<CoachingManifest> {
    return query.apply(store.list())
}
