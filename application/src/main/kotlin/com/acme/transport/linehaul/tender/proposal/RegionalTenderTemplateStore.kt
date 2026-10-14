package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps regional tender templates by code. */
interface RegionalTenderTemplateStore {
    fun find(code: String): RegionalTenderTemplate?
    fun save(item: RegionalTenderTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalTenderTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalTenderTemplateStore(
    var items: MutableMap<String, RegionalTenderTemplate>,
    var order: MutableList<String>,
) : RegionalTenderTemplateStore {
    override fun find(code: String): RegionalTenderTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalTenderTemplate) {
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

    override fun list(): MutableList<RegionalTenderTemplate> {
        val out = mutableListOf<RegionalTenderTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalTenderTemplateStore(): RegionalTenderTemplateStore {
    return MemoryRegionalTenderTemplateStore(
        mutableMapOf<String, RegionalTenderTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalTenderTemplateStore(
    var inner: RegionalTenderTemplateStore,
    var cache: TtlCache<RegionalTenderTemplate>,
    var hits: Int,
    var misses: Int,
) : RegionalTenderTemplateStore {
    override fun find(code: String): RegionalTenderTemplate? {
        val hit = (cache.get(code) as RegionalTenderTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalTenderTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalTenderTemplate> = inner.list()
}

fun newCachedRegionalTenderTemplateStore(inner: RegionalTenderTemplateStore, ttl: Int, clock: Clock): RegionalTenderTemplateStore {
    return CachedRegionalTenderTemplateStore(
        inner = inner,
        cache = newTtlCache<RegionalTenderTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional tender templates. Empty fields match all. */
data class RegionalTenderTemplateQuery(
    var segment: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalTenderTemplate): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalTenderTemplate>): MutableList<RegionalTenderTemplate> {
        val matched = filter(items) { candidate: RegionalTenderTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalTenderTemplates(store: RegionalTenderTemplateStore, query: RegionalTenderTemplateQuery): MutableList<RegionalTenderTemplate> {
    return query.apply(store.list())
}
