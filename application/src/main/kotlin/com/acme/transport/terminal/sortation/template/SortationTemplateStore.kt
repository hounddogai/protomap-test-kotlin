package com.acme.transport.terminal.sortation.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation templates by code. */
interface SortationTemplateStore {
    fun find(code: String): SortationTemplate?
    fun save(item: SortationTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationTemplateStore(
    var items: MutableMap<String, SortationTemplate>,
    var order: MutableList<String>,
) : SortationTemplateStore {
    override fun find(code: String): SortationTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationTemplate) {
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

    override fun list(): MutableList<SortationTemplate> {
        val out = mutableListOf<SortationTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationTemplateStore(): SortationTemplateStore {
    return MemorySortationTemplateStore(mutableMapOf<String, SortationTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationTemplateStore(
    var inner: SortationTemplateStore,
    var cache: TtlCache<SortationTemplate>,
    var hits: Int,
    var misses: Int,
) : SortationTemplateStore {
    override fun find(code: String): SortationTemplate? {
        val hit = (cache.get(code) as SortationTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationTemplate> = inner.list()
}

fun newCachedSortationTemplateStore(inner: SortationTemplateStore, ttl: Int, clock: Clock): SortationTemplateStore {
    return CachedSortationTemplateStore(
        inner = inner,
        cache = newTtlCache<SortationTemplate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation templates. Empty fields match all. */
data class SortationTemplateQuery(
    var depot: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationTemplate): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<SortationTemplate>): MutableList<SortationTemplate> {
        val matched = filter(items) { candidate: SortationTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationTemplates(store: SortationTemplateStore, query: SortationTemplateQuery): MutableList<SortationTemplate> {
    return query.apply(store.list())
}
