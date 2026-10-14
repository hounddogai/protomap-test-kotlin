package com.acme.transport.crewing.dutyperiod.mapping

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy duty period mappings by code. */
interface LegacyDutyPeriodMappingStore {
    fun find(code: String): LegacyDutyPeriodMapping?
    fun save(item: LegacyDutyPeriodMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyDutyPeriodMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyDutyPeriodMappingStore(
    var items: MutableMap<String, LegacyDutyPeriodMapping>,
    var order: MutableList<String>,
) : LegacyDutyPeriodMappingStore {
    override fun find(code: String): LegacyDutyPeriodMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyDutyPeriodMapping) {
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

    override fun list(): MutableList<LegacyDutyPeriodMapping> {
        val out = mutableListOf<LegacyDutyPeriodMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyDutyPeriodMappingStore(): LegacyDutyPeriodMappingStore {
    return MemoryLegacyDutyPeriodMappingStore(
        mutableMapOf<String, LegacyDutyPeriodMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyDutyPeriodMappingStore(
    var inner: LegacyDutyPeriodMappingStore,
    var cache: TtlCache<LegacyDutyPeriodMapping>,
    var hits: Int,
    var misses: Int,
) : LegacyDutyPeriodMappingStore {
    override fun find(code: String): LegacyDutyPeriodMapping? {
        val hit = (cache.get(code) as LegacyDutyPeriodMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyDutyPeriodMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyDutyPeriodMapping> = inner.list()
}

fun newCachedLegacyDutyPeriodMappingStore(inner: LegacyDutyPeriodMappingStore, ttl: Int, clock: Clock): LegacyDutyPeriodMappingStore {
    return CachedLegacyDutyPeriodMappingStore(
        inner = inner,
        cache = newTtlCache<LegacyDutyPeriodMapping>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy duty period mappings. Empty fields match all. */
data class LegacyDutyPeriodMappingQuery(
    var lane: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyDutyPeriodMapping): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyDutyPeriodMapping>): MutableList<LegacyDutyPeriodMapping> {
        val matched = filter(items) { candidate: LegacyDutyPeriodMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyDutyPeriodMappings(store: LegacyDutyPeriodMappingStore, query: LegacyDutyPeriodMappingQuery): MutableList<LegacyDutyPeriodMapping> {
    return query.apply(store.list())
}
