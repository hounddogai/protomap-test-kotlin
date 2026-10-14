package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period templates by code. */
interface DutyPeriodTemplateStore {
    fun find(code: String): DutyPeriodTemplate?
    fun save(item: DutyPeriodTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodTemplateStore(
    var items: MutableMap<String, DutyPeriodTemplate>,
    var order: MutableList<String>,
) : DutyPeriodTemplateStore {
    override fun find(code: String): DutyPeriodTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodTemplate) {
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

    override fun list(): MutableList<DutyPeriodTemplate> {
        val out = mutableListOf<DutyPeriodTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodTemplateStore(): DutyPeriodTemplateStore {
    return MemoryDutyPeriodTemplateStore(mutableMapOf<String, DutyPeriodTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodTemplateStore(
    var inner: DutyPeriodTemplateStore,
    var cache: TtlCache<DutyPeriodTemplate>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodTemplateStore {
    override fun find(code: String): DutyPeriodTemplate? {
        val hit = (cache.get(code) as DutyPeriodTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodTemplate> = inner.list()
}

fun newCachedDutyPeriodTemplateStore(inner: DutyPeriodTemplateStore, ttl: Int, clock: Clock): DutyPeriodTemplateStore {
    return CachedDutyPeriodTemplateStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodTemplate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period templates. Empty fields match all. */
data class DutyPeriodTemplateQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodTemplate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodTemplate>): MutableList<DutyPeriodTemplate> {
        val matched = filter(items) { candidate: DutyPeriodTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodTemplates(store: DutyPeriodTemplateStore, query: DutyPeriodTemplateQuery): MutableList<DutyPeriodTemplate> {
    return query.apply(store.list())
}
