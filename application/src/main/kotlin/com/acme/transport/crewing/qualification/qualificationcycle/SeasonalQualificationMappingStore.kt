package com.acme.transport.crewing.qualification.qualificationcycle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal qualification mappings by code. */
interface SeasonalQualificationMappingStore {
    fun find(code: String): SeasonalQualificationMapping?
    fun save(item: SeasonalQualificationMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalQualificationMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalQualificationMappingStore(
    var items: MutableMap<String, SeasonalQualificationMapping>,
    var order: MutableList<String>,
) : SeasonalQualificationMappingStore {
    override fun find(code: String): SeasonalQualificationMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalQualificationMapping) {
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

    override fun list(): MutableList<SeasonalQualificationMapping> {
        val out = mutableListOf<SeasonalQualificationMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalQualificationMappingStore(): SeasonalQualificationMappingStore {
    return MemorySeasonalQualificationMappingStore(
        mutableMapOf<String, SeasonalQualificationMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalQualificationMappingStore(
    var inner: SeasonalQualificationMappingStore,
    var cache: TtlCache<SeasonalQualificationMapping>,
    var hits: Int,
    var misses: Int,
) : SeasonalQualificationMappingStore {
    override fun find(code: String): SeasonalQualificationMapping? {
        val hit = (cache.get(code) as SeasonalQualificationMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalQualificationMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalQualificationMapping> = inner.list()
}

fun newCachedSeasonalQualificationMappingStore(inner: SeasonalQualificationMappingStore, ttl: Int, clock: Clock): SeasonalQualificationMappingStore {
    return CachedSeasonalQualificationMappingStore(
        inner = inner,
        cache = newTtlCache<SeasonalQualificationMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal qualification mappings. Empty fields match all. */
data class SeasonalQualificationMappingQuery(
    var source: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalQualificationMapping): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalQualificationMapping>): MutableList<SeasonalQualificationMapping> {
        val matched = filter(items) { candidate: SeasonalQualificationMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalQualificationMappings(store: SeasonalQualificationMappingStore, query: SeasonalQualificationMappingQuery): MutableList<SeasonalQualificationMapping> {
    return query.apply(store.list())
}
