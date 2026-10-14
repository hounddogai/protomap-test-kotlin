package com.acme.transport.terminal.dwell.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell templates by code. */
interface DwellTemplateStore {
    fun find(code: String): DwellTemplate?
    fun save(item: DwellTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellTemplateStore(
    var items: MutableMap<String, DwellTemplate>,
    var order: MutableList<String>,
) : DwellTemplateStore {
    override fun find(code: String): DwellTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellTemplate) {
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

    override fun list(): MutableList<DwellTemplate> {
        val out = mutableListOf<DwellTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellTemplateStore(): DwellTemplateStore {
    return MemoryDwellTemplateStore(mutableMapOf<String, DwellTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellTemplateStore(
    var inner: DwellTemplateStore,
    var cache: TtlCache<DwellTemplate>,
    var hits: Int,
    var misses: Int,
) : DwellTemplateStore {
    override fun find(code: String): DwellTemplate? {
        val hit = (cache.get(code) as DwellTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellTemplate> = inner.list()
}

fun newCachedDwellTemplateStore(inner: DwellTemplateStore, ttl: Int, clock: Clock): DwellTemplateStore {
    return CachedDwellTemplateStore(
        inner = inner,
        cache = newTtlCache<DwellTemplate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell templates. Empty fields match all. */
data class DwellTemplateQuery(
    var category: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellTemplate): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellTemplate>): MutableList<DwellTemplate> {
        val matched = filter(items) { candidate: DwellTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellTemplates(store: DwellTemplateStore, query: DwellTemplateQuery): MutableList<DwellTemplate> {
    return query.apply(store.list())
}
