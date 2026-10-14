package com.acme.transport.crewing.dutyperiod.assignment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period targets by code. */
interface DutyPeriodTargetStore {
    fun find(code: String): DutyPeriodTarget?
    fun save(item: DutyPeriodTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodTargetStore(
    var items: MutableMap<String, DutyPeriodTarget>,
    var order: MutableList<String>,
) : DutyPeriodTargetStore {
    override fun find(code: String): DutyPeriodTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodTarget) {
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

    override fun list(): MutableList<DutyPeriodTarget> {
        val out = mutableListOf<DutyPeriodTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodTargetStore(): DutyPeriodTargetStore {
    return MemoryDutyPeriodTargetStore(mutableMapOf<String, DutyPeriodTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodTargetStore(
    var inner: DutyPeriodTargetStore,
    var cache: TtlCache<DutyPeriodTarget>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodTargetStore {
    override fun find(code: String): DutyPeriodTarget? {
        val hit = (cache.get(code) as DutyPeriodTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodTarget> = inner.list()
}

fun newCachedDutyPeriodTargetStore(inner: DutyPeriodTargetStore, ttl: Int, clock: Clock): DutyPeriodTargetStore {
    return CachedDutyPeriodTargetStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodTarget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period targets. Empty fields match all. */
data class DutyPeriodTargetQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodTarget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodTarget>): MutableList<DutyPeriodTarget> {
        val matched = filter(items) { candidate: DutyPeriodTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodTargets(store: DutyPeriodTargetStore, query: DutyPeriodTargetQuery): MutableList<DutyPeriodTarget> {
    return query.apply(store.list())
}
