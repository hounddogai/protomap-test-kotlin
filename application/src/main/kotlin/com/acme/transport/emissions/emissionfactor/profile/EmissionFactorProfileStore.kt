package com.acme.transport.emissions.emissionfactor.profile

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor profiles by code. */
interface EmissionFactorProfileStore {
    fun find(code: String): EmissionFactorProfile?
    fun save(item: EmissionFactorProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorProfileStore(
    var items: MutableMap<String, EmissionFactorProfile>,
    var order: MutableList<String>,
) : EmissionFactorProfileStore {
    override fun find(code: String): EmissionFactorProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorProfile) {
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

    override fun list(): MutableList<EmissionFactorProfile> {
        val out = mutableListOf<EmissionFactorProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorProfileStore(): EmissionFactorProfileStore {
    return MemoryEmissionFactorProfileStore(
        mutableMapOf<String, EmissionFactorProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorProfileStore(
    var inner: EmissionFactorProfileStore,
    var cache: TtlCache<EmissionFactorProfile>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorProfileStore {
    override fun find(code: String): EmissionFactorProfile? {
        val hit = (cache.get(code) as EmissionFactorProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorProfile> = inner.list()
}

fun newCachedEmissionFactorProfileStore(inner: EmissionFactorProfileStore, ttl: Int, clock: Clock): EmissionFactorProfileStore {
    return CachedEmissionFactorProfileStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor profiles. Empty fields match all. */
data class EmissionFactorProfileQuery(
    var unit: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorProfile): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorProfile>): MutableList<EmissionFactorProfile> {
        val matched = filter(items) { candidate: EmissionFactorProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorProfiles(store: EmissionFactorProfileStore, query: EmissionFactorProfileQuery): MutableList<EmissionFactorProfile> {
    return query.apply(store.list())
}
