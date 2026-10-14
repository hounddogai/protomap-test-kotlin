package com.acme.transport.crewing.availability.primaryavailabilitytier

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy availability templates by code. */
interface LegacyAvailabilityTemplateStore {
    fun find(code: String): LegacyAvailabilityTemplate?
    fun save(item: LegacyAvailabilityTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyAvailabilityTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyAvailabilityTemplateStore(
    var items: MutableMap<String, LegacyAvailabilityTemplate>,
    var order: MutableList<String>,
) : LegacyAvailabilityTemplateStore {
    override fun find(code: String): LegacyAvailabilityTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyAvailabilityTemplate) {
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

    override fun list(): MutableList<LegacyAvailabilityTemplate> {
        val out = mutableListOf<LegacyAvailabilityTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyAvailabilityTemplateStore(): LegacyAvailabilityTemplateStore {
    return MemoryLegacyAvailabilityTemplateStore(
        mutableMapOf<String, LegacyAvailabilityTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyAvailabilityTemplateStore(
    var inner: LegacyAvailabilityTemplateStore,
    var cache: TtlCache<LegacyAvailabilityTemplate>,
    var hits: Int,
    var misses: Int,
) : LegacyAvailabilityTemplateStore {
    override fun find(code: String): LegacyAvailabilityTemplate? {
        val hit = (cache.get(code) as LegacyAvailabilityTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyAvailabilityTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyAvailabilityTemplate> = inner.list()
}

fun newCachedLegacyAvailabilityTemplateStore(inner: LegacyAvailabilityTemplateStore, ttl: Int, clock: Clock): LegacyAvailabilityTemplateStore {
    return CachedLegacyAvailabilityTemplateStore(
        inner = inner,
        cache = newTtlCache<LegacyAvailabilityTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy availability templates. Empty fields match all. */
data class LegacyAvailabilityTemplateQuery(
    var name: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyAvailabilityTemplate): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<LegacyAvailabilityTemplate>): MutableList<LegacyAvailabilityTemplate> {
        val matched = filter(items) { candidate: LegacyAvailabilityTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyAvailabilityTemplates(store: LegacyAvailabilityTemplateStore, query: LegacyAvailabilityTemplateQuery): MutableList<LegacyAvailabilityTemplate> {
    return query.apply(store.list())
}
