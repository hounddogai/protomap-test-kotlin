package com.acme.transport.terminal.sortation.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps adaptive sortation templates by code. */
interface AdaptiveSortationTemplateStore {
    fun find(code: String): AdaptiveSortationTemplate?
    fun save(item: AdaptiveSortationTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveSortationTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveSortationTemplateStore(
    var items: MutableMap<String, AdaptiveSortationTemplate>,
    var order: MutableList<String>,
) : AdaptiveSortationTemplateStore {
    override fun find(code: String): AdaptiveSortationTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveSortationTemplate) {
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

    override fun list(): MutableList<AdaptiveSortationTemplate> {
        val out = mutableListOf<AdaptiveSortationTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveSortationTemplateStore(): AdaptiveSortationTemplateStore {
    return MemoryAdaptiveSortationTemplateStore(
        mutableMapOf<String, AdaptiveSortationTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveSortationTemplateStore(
    var inner: AdaptiveSortationTemplateStore,
    var cache: TtlCache<AdaptiveSortationTemplate>,
    var hits: Int,
    var misses: Int,
) : AdaptiveSortationTemplateStore {
    override fun find(code: String): AdaptiveSortationTemplate? {
        val hit = (cache.get(code) as AdaptiveSortationTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveSortationTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveSortationTemplate> = inner.list()
}

fun newCachedAdaptiveSortationTemplateStore(inner: AdaptiveSortationTemplateStore, ttl: Int, clock: Clock): AdaptiveSortationTemplateStore {
    return CachedAdaptiveSortationTemplateStore(
        inner = inner,
        cache = newTtlCache<AdaptiveSortationTemplate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive sortation templates. Empty fields match all. */
data class AdaptiveSortationTemplateQuery(
    var locale: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveSortationTemplate): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveSortationTemplate>): MutableList<AdaptiveSortationTemplate> {
        val matched = filter(items) { candidate: AdaptiveSortationTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveSortationTemplates(store: AdaptiveSortationTemplateStore, query: AdaptiveSortationTemplateQuery): MutableList<AdaptiveSortationTemplate> {
    return query.apply(store.list())
}
