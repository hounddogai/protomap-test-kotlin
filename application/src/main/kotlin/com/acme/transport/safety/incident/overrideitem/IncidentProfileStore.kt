package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident profiles by code. */
interface IncidentProfileStore {
    fun find(code: String): IncidentProfile?
    fun save(item: IncidentProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentProfileStore(
    var items: MutableMap<String, IncidentProfile>,
    var order: MutableList<String>,
) : IncidentProfileStore {
    override fun find(code: String): IncidentProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentProfile) {
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

    override fun list(): MutableList<IncidentProfile> {
        val out = mutableListOf<IncidentProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentProfileStore(): IncidentProfileStore {
    return MemoryIncidentProfileStore(mutableMapOf<String, IncidentProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentProfileStore(
    var inner: IncidentProfileStore,
    var cache: TtlCache<IncidentProfile>,
    var hits: Int,
    var misses: Int,
) : IncidentProfileStore {
    override fun find(code: String): IncidentProfile? {
        val hit = (cache.get(code) as IncidentProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentProfile> = inner.list()
}

fun newCachedIncidentProfileStore(inner: IncidentProfileStore, ttl: Int, clock: Clock): IncidentProfileStore {
    return CachedIncidentProfileStore(
        inner = inner,
        cache = newTtlCache<IncidentProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident profiles. Empty fields match all. */
data class IncidentProfileQuery(
    var lane: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentProfile): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<IncidentProfile>): MutableList<IncidentProfile> {
        val matched = filter(items) { candidate: IncidentProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentProfiles(store: IncidentProfileStore, query: IncidentProfileQuery): MutableList<IncidentProfile> {
    return query.apply(store.list())
}
