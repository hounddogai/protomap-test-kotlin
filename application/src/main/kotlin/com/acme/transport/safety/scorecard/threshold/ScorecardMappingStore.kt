package com.acme.transport.safety.scorecard.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard mappings by code. */
interface ScorecardMappingStore {
    fun find(code: String): ScorecardMapping?
    fun save(item: ScorecardMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardMappingStore(
    var items: MutableMap<String, ScorecardMapping>,
    var order: MutableList<String>,
) : ScorecardMappingStore {
    override fun find(code: String): ScorecardMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardMapping) {
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

    override fun list(): MutableList<ScorecardMapping> {
        val out = mutableListOf<ScorecardMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardMappingStore(): ScorecardMappingStore {
    return MemoryScorecardMappingStore(mutableMapOf<String, ScorecardMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardMappingStore(
    var inner: ScorecardMappingStore,
    var cache: TtlCache<ScorecardMapping>,
    var hits: Int,
    var misses: Int,
) : ScorecardMappingStore {
    override fun find(code: String): ScorecardMapping? {
        val hit = (cache.get(code) as ScorecardMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardMapping> = inner.list()
}

fun newCachedScorecardMappingStore(inner: ScorecardMappingStore, ttl: Int, clock: Clock): ScorecardMappingStore {
    return CachedScorecardMappingStore(
        inner = inner,
        cache = newTtlCache<ScorecardMapping>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard mappings. Empty fields match all. */
data class ScorecardMappingQuery(
    var label: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardMapping): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScorecardMapping>): MutableList<ScorecardMapping> {
        val matched = filter(items) { candidate: ScorecardMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardMappings(store: ScorecardMappingStore, query: ScorecardMappingQuery): MutableList<ScorecardMapping> {
    return query.apply(store.list())
}
