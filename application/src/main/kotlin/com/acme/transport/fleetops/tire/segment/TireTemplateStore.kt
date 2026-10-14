package com.acme.transport.fleetops.tire.segment

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire templates by code. */
interface TireTemplateStore {
    fun find(code: String): TireTemplate?
    fun save(item: TireTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireTemplateStore(
    var items: MutableMap<String, TireTemplate>,
    var order: MutableList<String>,
) : TireTemplateStore {
    override fun find(code: String): TireTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireTemplate) {
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

    override fun list(): MutableList<TireTemplate> {
        val out = mutableListOf<TireTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireTemplateStore(): TireTemplateStore {
    return MemoryTireTemplateStore(mutableMapOf<String, TireTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireTemplateStore(
    var inner: TireTemplateStore,
    var cache: TtlCache<TireTemplate>,
    var hits: Int,
    var misses: Int,
) : TireTemplateStore {
    override fun find(code: String): TireTemplate? {
        val hit = (cache.get(code) as TireTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireTemplate> = inner.list()
}

fun newCachedTireTemplateStore(inner: TireTemplateStore, ttl: Int, clock: Clock): TireTemplateStore {
    return CachedTireTemplateStore(
        inner = inner,
        cache = newTtlCache<TireTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire templates. Empty fields match all. */
data class TireTemplateQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireTemplate): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireTemplate>): MutableList<TireTemplate> {
        val matched = filter(items) { candidate: TireTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireTemplates(store: TireTemplateStore, query: TireTemplateQuery): MutableList<TireTemplate> {
    return query.apply(store.list())
}
