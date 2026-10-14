package com.acme.transport.crewing.restbreak.summary

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break templates by code. */
interface RestBreakTemplateStore {
    fun find(code: String): RestBreakTemplate?
    fun save(item: RestBreakTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakTemplateStore(
    var items: MutableMap<String, RestBreakTemplate>,
    var order: MutableList<String>,
) : RestBreakTemplateStore {
    override fun find(code: String): RestBreakTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakTemplate) {
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

    override fun list(): MutableList<RestBreakTemplate> {
        val out = mutableListOf<RestBreakTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakTemplateStore(): RestBreakTemplateStore {
    return MemoryRestBreakTemplateStore(mutableMapOf<String, RestBreakTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakTemplateStore(
    var inner: RestBreakTemplateStore,
    var cache: TtlCache<RestBreakTemplate>,
    var hits: Int,
    var misses: Int,
) : RestBreakTemplateStore {
    override fun find(code: String): RestBreakTemplate? {
        val hit = (cache.get(code) as RestBreakTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakTemplate> = inner.list()
}

fun newCachedRestBreakTemplateStore(inner: RestBreakTemplateStore, ttl: Int, clock: Clock): RestBreakTemplateStore {
    return CachedRestBreakTemplateStore(
        inner = inner,
        cache = newTtlCache<RestBreakTemplate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break templates. Empty fields match all. */
data class RestBreakTemplateQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakTemplate): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakTemplate>): MutableList<RestBreakTemplate> {
        val matched = filter(items) { candidate: RestBreakTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakTemplates(store: RestBreakTemplateStore, query: RestBreakTemplateQuery): MutableList<RestBreakTemplate> {
    return query.apply(store.list())
}
