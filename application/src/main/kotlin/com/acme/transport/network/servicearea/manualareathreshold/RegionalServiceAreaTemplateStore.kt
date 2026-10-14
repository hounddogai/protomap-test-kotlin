package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional service area templates by code. */
interface RegionalServiceAreaTemplateStore {
    fun find(code: String): RegionalServiceAreaTemplate?
    fun save(item: RegionalServiceAreaTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalServiceAreaTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalServiceAreaTemplateStore(
    var items: MutableMap<String, RegionalServiceAreaTemplate>,
    var order: MutableList<String>,
) : RegionalServiceAreaTemplateStore {
    override fun find(code: String): RegionalServiceAreaTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalServiceAreaTemplate) {
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

    override fun list(): MutableList<RegionalServiceAreaTemplate> {
        val out = mutableListOf<RegionalServiceAreaTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalServiceAreaTemplateStore(): RegionalServiceAreaTemplateStore {
    return MemoryRegionalServiceAreaTemplateStore(
        mutableMapOf<String, RegionalServiceAreaTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalServiceAreaTemplateStore(
    var inner: RegionalServiceAreaTemplateStore,
    var cache: TtlCache<RegionalServiceAreaTemplate>,
    var hits: Int,
    var misses: Int,
) : RegionalServiceAreaTemplateStore {
    override fun find(code: String): RegionalServiceAreaTemplate? {
        val hit = (cache.get(code) as RegionalServiceAreaTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalServiceAreaTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalServiceAreaTemplate> = inner.list()
}

fun newCachedRegionalServiceAreaTemplateStore(inner: RegionalServiceAreaTemplateStore, ttl: Int, clock: Clock): RegionalServiceAreaTemplateStore {
    return CachedRegionalServiceAreaTemplateStore(
        inner = inner,
        cache = newTtlCache<RegionalServiceAreaTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional service area templates. Empty fields match all. */
data class RegionalServiceAreaTemplateQuery(
    var country: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalServiceAreaTemplate): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalServiceAreaTemplate>): MutableList<RegionalServiceAreaTemplate> {
        val matched = filter(items) { candidate: RegionalServiceAreaTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalServiceAreaTemplates(store: RegionalServiceAreaTemplateStore, query: RegionalServiceAreaTemplateQuery): MutableList<RegionalServiceAreaTemplate> {
    return query.apply(store.list())
}
