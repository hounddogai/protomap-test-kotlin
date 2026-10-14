package com.acme.transport.crewing.qualification.qualificationoverride

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive qualification overrides by code. */
interface AdaptiveQualificationOverrideStore {
    fun find(code: String): AdaptiveQualificationOverride?
    fun save(item: AdaptiveQualificationOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveQualificationOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveQualificationOverrideStore(
    var items: MutableMap<String, AdaptiveQualificationOverride>,
    var order: MutableList<String>,
) : AdaptiveQualificationOverrideStore {
    override fun find(code: String): AdaptiveQualificationOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveQualificationOverride) {
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

    override fun list(): MutableList<AdaptiveQualificationOverride> {
        val out = mutableListOf<AdaptiveQualificationOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveQualificationOverrideStore(): AdaptiveQualificationOverrideStore {
    return MemoryAdaptiveQualificationOverrideStore(
        mutableMapOf<String, AdaptiveQualificationOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveQualificationOverrideStore(
    var inner: AdaptiveQualificationOverrideStore,
    var cache: TtlCache<AdaptiveQualificationOverride>,
    var hits: Int,
    var misses: Int,
) : AdaptiveQualificationOverrideStore {
    override fun find(code: String): AdaptiveQualificationOverride? {
        val hit = (cache.get(code) as AdaptiveQualificationOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveQualificationOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveQualificationOverride> = inner.list()
}

fun newCachedAdaptiveQualificationOverrideStore(inner: AdaptiveQualificationOverrideStore, ttl: Int, clock: Clock): AdaptiveQualificationOverrideStore {
    return CachedAdaptiveQualificationOverrideStore(
        inner = inner,
        cache = newTtlCache<AdaptiveQualificationOverride>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive qualification overrides. Empty fields match all. */
data class AdaptiveQualificationOverrideQuery(
    var lane: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveQualificationOverride): Boolean {
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

    fun apply(items: MutableList<AdaptiveQualificationOverride>): MutableList<AdaptiveQualificationOverride> {
        val matched = filter(items) { candidate: AdaptiveQualificationOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveQualificationOverrides(store: AdaptiveQualificationOverrideStore, query: AdaptiveQualificationOverrideQuery): MutableList<AdaptiveQualificationOverride> {
    return query.apply(store.list())
}
