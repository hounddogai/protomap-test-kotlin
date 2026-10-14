package com.acme.transport.emissions.carbonreport.template

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report templates by code. */
interface CarbonReportTemplateStore {
    fun find(code: String): CarbonReportTemplate?
    fun save(item: CarbonReportTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportTemplateStore(
    var items: MutableMap<String, CarbonReportTemplate>,
    var order: MutableList<String>,
) : CarbonReportTemplateStore {
    override fun find(code: String): CarbonReportTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportTemplate) {
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

    override fun list(): MutableList<CarbonReportTemplate> {
        val out = mutableListOf<CarbonReportTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportTemplateStore(): CarbonReportTemplateStore {
    return MemoryCarbonReportTemplateStore(
        mutableMapOf<String, CarbonReportTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportTemplateStore(
    var inner: CarbonReportTemplateStore,
    var cache: TtlCache<CarbonReportTemplate>,
    var hits: Int,
    var misses: Int,
) : CarbonReportTemplateStore {
    override fun find(code: String): CarbonReportTemplate? {
        val hit = (cache.get(code) as CarbonReportTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportTemplate> = inner.list()
}

fun newCachedCarbonReportTemplateStore(inner: CarbonReportTemplateStore, ttl: Int, clock: Clock): CarbonReportTemplateStore {
    return CachedCarbonReportTemplateStore(
        inner = inner,
        cache = newTtlCache<CarbonReportTemplate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report templates. Empty fields match all. */
data class CarbonReportTemplateQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportTemplate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportTemplate>): MutableList<CarbonReportTemplate> {
        val matched = filter(items) { candidate: CarbonReportTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportTemplates(store: CarbonReportTemplateStore, query: CarbonReportTemplateQuery): MutableList<CarbonReportTemplate> {
    return query.apply(store.list())
}
