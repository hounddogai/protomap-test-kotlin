package com.acme.transport.terminal.crossdock.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock templates by code. */
interface CrossDockTemplateStore {
    fun find(code: String): CrossDockTemplate?
    fun save(item: CrossDockTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockTemplateStore(
    var items: MutableMap<String, CrossDockTemplate>,
    var order: MutableList<String>,
) : CrossDockTemplateStore {
    override fun find(code: String): CrossDockTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockTemplate) {
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

    override fun list(): MutableList<CrossDockTemplate> {
        val out = mutableListOf<CrossDockTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockTemplateStore(): CrossDockTemplateStore {
    return MemoryCrossDockTemplateStore(mutableMapOf<String, CrossDockTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockTemplateStore(
    var inner: CrossDockTemplateStore,
    var cache: TtlCache<CrossDockTemplate>,
    var hits: Int,
    var misses: Int,
) : CrossDockTemplateStore {
    override fun find(code: String): CrossDockTemplate? {
        val hit = (cache.get(code) as CrossDockTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockTemplate> = inner.list()
}

fun newCachedCrossDockTemplateStore(inner: CrossDockTemplateStore, ttl: Int, clock: Clock): CrossDockTemplateStore {
    return CachedCrossDockTemplateStore(
        inner = inner,
        cache = newTtlCache<CrossDockTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock templates. Empty fields match all. */
data class CrossDockTemplateQuery(
    var unit: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockTemplate): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<CrossDockTemplate>): MutableList<CrossDockTemplate> {
        val matched = filter(items) { candidate: CrossDockTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockTemplates(store: CrossDockTemplateStore, query: CrossDockTemplateQuery): MutableList<CrossDockTemplate> {
    return query.apply(store.list())
}
