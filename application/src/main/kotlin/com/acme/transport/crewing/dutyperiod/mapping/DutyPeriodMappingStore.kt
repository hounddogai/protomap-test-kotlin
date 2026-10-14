package com.acme.transport.crewing.dutyperiod.mapping

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period mappings by code. */
interface DutyPeriodMappingStore {
    fun find(code: String): DutyPeriodMapping?
    fun save(item: DutyPeriodMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodMappingStore(
    var items: MutableMap<String, DutyPeriodMapping>,
    var order: MutableList<String>,
) : DutyPeriodMappingStore {
    override fun find(code: String): DutyPeriodMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodMapping) {
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

    override fun list(): MutableList<DutyPeriodMapping> {
        val out = mutableListOf<DutyPeriodMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodMappingStore(): DutyPeriodMappingStore {
    return MemoryDutyPeriodMappingStore(mutableMapOf<String, DutyPeriodMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodMappingStore(
    var inner: DutyPeriodMappingStore,
    var cache: TtlCache<DutyPeriodMapping>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodMappingStore {
    override fun find(code: String): DutyPeriodMapping? {
        val hit = (cache.get(code) as DutyPeriodMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodMapping> = inner.list()
}

fun newCachedDutyPeriodMappingStore(inner: DutyPeriodMappingStore, ttl: Int, clock: Clock): DutyPeriodMappingStore {
    return CachedDutyPeriodMappingStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period mappings. Empty fields match all. */
data class DutyPeriodMappingQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodMapping): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodMapping>): MutableList<DutyPeriodMapping> {
        val matched = filter(items) { candidate: DutyPeriodMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodMappings(store: DutyPeriodMappingStore, query: DutyPeriodMappingQuery): MutableList<DutyPeriodMapping> {
    return query.apply(store.list())
}
