package com.acme.transport.customs.inspection.entry

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive inspection profiles by code. */
interface AdaptiveInspectionProfileStore {
    fun find(code: String): AdaptiveInspectionProfile?
    fun save(item: AdaptiveInspectionProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveInspectionProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveInspectionProfileStore(
    var items: MutableMap<String, AdaptiveInspectionProfile>,
    var order: MutableList<String>,
) : AdaptiveInspectionProfileStore {
    override fun find(code: String): AdaptiveInspectionProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveInspectionProfile) {
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

    override fun list(): MutableList<AdaptiveInspectionProfile> {
        val out = mutableListOf<AdaptiveInspectionProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveInspectionProfileStore(): AdaptiveInspectionProfileStore {
    return MemoryAdaptiveInspectionProfileStore(
        mutableMapOf<String, AdaptiveInspectionProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveInspectionProfileStore(
    var inner: AdaptiveInspectionProfileStore,
    var cache: TtlCache<AdaptiveInspectionProfile>,
    var hits: Int,
    var misses: Int,
) : AdaptiveInspectionProfileStore {
    override fun find(code: String): AdaptiveInspectionProfile? {
        val hit = (cache.get(code) as AdaptiveInspectionProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveInspectionProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveInspectionProfile> = inner.list()
}

fun newCachedAdaptiveInspectionProfileStore(inner: AdaptiveInspectionProfileStore, ttl: Int, clock: Clock): AdaptiveInspectionProfileStore {
    return CachedAdaptiveInspectionProfileStore(
        inner = inner,
        cache = newTtlCache<AdaptiveInspectionProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive inspection profiles. Empty fields match all. */
data class AdaptiveInspectionProfileQuery(
    var name: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveInspectionProfile): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveInspectionProfile>): MutableList<AdaptiveInspectionProfile> {
        val matched = filter(items) { candidate: AdaptiveInspectionProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveInspectionProfiles(store: AdaptiveInspectionProfileStore, query: AdaptiveInspectionProfileQuery): MutableList<AdaptiveInspectionProfile> {
    return query.apply(store.list())
}
