package com.acme.transport.fleetops.tire.cycle

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary tire templates by code. */
interface PrimaryTireTemplateStore {
    fun find(code: String): PrimaryTireTemplate?
    fun save(item: PrimaryTireTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTireTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTireTemplateStore(
    var items: MutableMap<String, PrimaryTireTemplate>,
    var order: MutableList<String>,
) : PrimaryTireTemplateStore {
    override fun find(code: String): PrimaryTireTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTireTemplate) {
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

    override fun list(): MutableList<PrimaryTireTemplate> {
        val out = mutableListOf<PrimaryTireTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTireTemplateStore(): PrimaryTireTemplateStore {
    return MemoryPrimaryTireTemplateStore(mutableMapOf<String, PrimaryTireTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTireTemplateStore(
    var inner: PrimaryTireTemplateStore,
    var cache: TtlCache<PrimaryTireTemplate>,
    var hits: Int,
    var misses: Int,
) : PrimaryTireTemplateStore {
    override fun find(code: String): PrimaryTireTemplate? {
        val hit = (cache.get(code) as PrimaryTireTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTireTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTireTemplate> = inner.list()
}

fun newCachedPrimaryTireTemplateStore(inner: PrimaryTireTemplateStore, ttl: Int, clock: Clock): PrimaryTireTemplateStore {
    return CachedPrimaryTireTemplateStore(
        inner = inner,
        cache = newTtlCache<PrimaryTireTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary tire templates. Empty fields match all. */
data class PrimaryTireTemplateQuery(
    var category: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTireTemplate): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTireTemplate>): MutableList<PrimaryTireTemplate> {
        val matched = filter(items) { candidate: PrimaryTireTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTireTemplates(store: PrimaryTireTemplateStore, query: PrimaryTireTemplateQuery): MutableList<PrimaryTireTemplate> {
    return query.apply(store.list())
}
