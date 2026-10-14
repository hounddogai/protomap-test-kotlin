package com.acme.transport.safety.scorecard.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scheduled scorecard mappings by code. */
interface ScheduledScorecardMappingStore {
    fun find(code: String): ScheduledScorecardMapping?
    fun save(item: ScheduledScorecardMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledScorecardMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledScorecardMappingStore(
    var items: MutableMap<String, ScheduledScorecardMapping>,
    var order: MutableList<String>,
) : ScheduledScorecardMappingStore {
    override fun find(code: String): ScheduledScorecardMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledScorecardMapping) {
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

    override fun list(): MutableList<ScheduledScorecardMapping> {
        val out = mutableListOf<ScheduledScorecardMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledScorecardMappingStore(): ScheduledScorecardMappingStore {
    return MemoryScheduledScorecardMappingStore(
        mutableMapOf<String, ScheduledScorecardMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledScorecardMappingStore(
    var inner: ScheduledScorecardMappingStore,
    var cache: TtlCache<ScheduledScorecardMapping>,
    var hits: Int,
    var misses: Int,
) : ScheduledScorecardMappingStore {
    override fun find(code: String): ScheduledScorecardMapping? {
        val hit = (cache.get(code) as ScheduledScorecardMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledScorecardMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledScorecardMapping> = inner.list()
}

fun newCachedScheduledScorecardMappingStore(inner: ScheduledScorecardMappingStore, ttl: Int, clock: Clock): ScheduledScorecardMappingStore {
    return CachedScheduledScorecardMappingStore(
        inner = inner,
        cache = newTtlCache<ScheduledScorecardMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled scorecard mappings. Empty fields match all. */
data class ScheduledScorecardMappingQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledScorecardMapping): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ScheduledScorecardMapping>): MutableList<ScheduledScorecardMapping> {
        val matched = filter(items) { candidate: ScheduledScorecardMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledScorecardMappings(store: ScheduledScorecardMappingStore, query: ScheduledScorecardMappingQuery): MutableList<ScheduledScorecardMapping> {
    return query.apply(store.list())
}
