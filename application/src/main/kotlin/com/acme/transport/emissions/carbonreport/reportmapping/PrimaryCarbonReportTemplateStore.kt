package com.acme.transport.emissions.carbonreport.reportmapping

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary carbon report templates by code. */
interface PrimaryCarbonReportTemplateStore {
    fun find(code: String): PrimaryCarbonReportTemplate?
    fun save(item: PrimaryCarbonReportTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCarbonReportTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCarbonReportTemplateStore(
    var items: MutableMap<String, PrimaryCarbonReportTemplate>,
    var order: MutableList<String>,
) : PrimaryCarbonReportTemplateStore {
    override fun find(code: String): PrimaryCarbonReportTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCarbonReportTemplate) {
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

    override fun list(): MutableList<PrimaryCarbonReportTemplate> {
        val out = mutableListOf<PrimaryCarbonReportTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCarbonReportTemplateStore(): PrimaryCarbonReportTemplateStore {
    return MemoryPrimaryCarbonReportTemplateStore(
        mutableMapOf<String, PrimaryCarbonReportTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCarbonReportTemplateStore(
    var inner: PrimaryCarbonReportTemplateStore,
    var cache: TtlCache<PrimaryCarbonReportTemplate>,
    var hits: Int,
    var misses: Int,
) : PrimaryCarbonReportTemplateStore {
    override fun find(code: String): PrimaryCarbonReportTemplate? {
        val hit = (cache.get(code) as PrimaryCarbonReportTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCarbonReportTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCarbonReportTemplate> = inner.list()
}

fun newCachedPrimaryCarbonReportTemplateStore(inner: PrimaryCarbonReportTemplateStore, ttl: Int, clock: Clock): PrimaryCarbonReportTemplateStore {
    return CachedPrimaryCarbonReportTemplateStore(
        inner = inner,
        cache = newTtlCache<PrimaryCarbonReportTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary carbon report templates. Empty fields match all. */
data class PrimaryCarbonReportTemplateQuery(
    var locale: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCarbonReportTemplate): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<PrimaryCarbonReportTemplate>): MutableList<PrimaryCarbonReportTemplate> {
        val matched = filter(items) { candidate: PrimaryCarbonReportTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCarbonReportTemplates(store: PrimaryCarbonReportTemplateStore, query: PrimaryCarbonReportTemplateQuery): MutableList<PrimaryCarbonReportTemplate> {
    return query.apply(store.list())
}
