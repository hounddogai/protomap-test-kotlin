package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps scheduled zone templates by code. */
interface ScheduledZoneTemplateStore {
    fun find(code: String): ScheduledZoneTemplate?
    fun save(item: ScheduledZoneTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledZoneTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledZoneTemplateStore(
    var items: MutableMap<String, ScheduledZoneTemplate>,
    var order: MutableList<String>,
) : ScheduledZoneTemplateStore {
    override fun find(code: String): ScheduledZoneTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledZoneTemplate) {
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

    override fun list(): MutableList<ScheduledZoneTemplate> {
        val out = mutableListOf<ScheduledZoneTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledZoneTemplateStore(): ScheduledZoneTemplateStore {
    return MemoryScheduledZoneTemplateStore(
        mutableMapOf<String, ScheduledZoneTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledZoneTemplateStore(
    var inner: ScheduledZoneTemplateStore,
    var cache: TtlCache<ScheduledZoneTemplate>,
    var hits: Int,
    var misses: Int,
) : ScheduledZoneTemplateStore {
    override fun find(code: String): ScheduledZoneTemplate? {
        val hit = (cache.get(code) as ScheduledZoneTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledZoneTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledZoneTemplate> = inner.list()
}

fun newCachedScheduledZoneTemplateStore(inner: ScheduledZoneTemplateStore, ttl: Int, clock: Clock): ScheduledZoneTemplateStore {
    return CachedScheduledZoneTemplateStore(
        inner = inner,
        cache = newTtlCache<ScheduledZoneTemplate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled zone templates. Empty fields match all. */
data class ScheduledZoneTemplateQuery(
    var source: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledZoneTemplate): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledZoneTemplate>): MutableList<ScheduledZoneTemplate> {
        val matched = filter(items) { candidate: ScheduledZoneTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledZoneTemplates(store: ScheduledZoneTemplateStore, query: ScheduledZoneTemplateQuery): MutableList<ScheduledZoneTemplate> {
    return query.apply(store.list())
}
