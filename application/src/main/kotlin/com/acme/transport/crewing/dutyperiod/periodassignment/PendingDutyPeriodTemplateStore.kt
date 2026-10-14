package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending duty period templates by code. */
interface PendingDutyPeriodTemplateStore {
    fun find(code: String): PendingDutyPeriodTemplate?
    fun save(item: PendingDutyPeriodTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingDutyPeriodTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingDutyPeriodTemplateStore(
    var items: MutableMap<String, PendingDutyPeriodTemplate>,
    var order: MutableList<String>,
) : PendingDutyPeriodTemplateStore {
    override fun find(code: String): PendingDutyPeriodTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingDutyPeriodTemplate) {
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

    override fun list(): MutableList<PendingDutyPeriodTemplate> {
        val out = mutableListOf<PendingDutyPeriodTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingDutyPeriodTemplateStore(): PendingDutyPeriodTemplateStore {
    return MemoryPendingDutyPeriodTemplateStore(
        mutableMapOf<String, PendingDutyPeriodTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingDutyPeriodTemplateStore(
    var inner: PendingDutyPeriodTemplateStore,
    var cache: TtlCache<PendingDutyPeriodTemplate>,
    var hits: Int,
    var misses: Int,
) : PendingDutyPeriodTemplateStore {
    override fun find(code: String): PendingDutyPeriodTemplate? {
        val hit = (cache.get(code) as PendingDutyPeriodTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingDutyPeriodTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingDutyPeriodTemplate> = inner.list()
}

fun newCachedPendingDutyPeriodTemplateStore(inner: PendingDutyPeriodTemplateStore, ttl: Int, clock: Clock): PendingDutyPeriodTemplateStore {
    return CachedPendingDutyPeriodTemplateStore(
        inner = inner,
        cache = newTtlCache<PendingDutyPeriodTemplate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending duty period templates. Empty fields match all. */
data class PendingDutyPeriodTemplateQuery(
    var depot: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingDutyPeriodTemplate): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingDutyPeriodTemplate>): MutableList<PendingDutyPeriodTemplate> {
        val matched = filter(items) { candidate: PendingDutyPeriodTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingDutyPeriodTemplates(store: PendingDutyPeriodTemplateStore, query: PendingDutyPeriodTemplateQuery): MutableList<PendingDutyPeriodTemplate> {
    return query.apply(store.list())
}
