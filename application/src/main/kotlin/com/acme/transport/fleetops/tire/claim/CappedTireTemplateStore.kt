package com.acme.transport.fleetops.tire.claim

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped tire templates by code. */
interface CappedTireTemplateStore {
    fun find(code: String): CappedTireTemplate?
    fun save(item: CappedTireTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedTireTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedTireTemplateStore(
    var items: MutableMap<String, CappedTireTemplate>,
    var order: MutableList<String>,
) : CappedTireTemplateStore {
    override fun find(code: String): CappedTireTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedTireTemplate) {
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

    override fun list(): MutableList<CappedTireTemplate> {
        val out = mutableListOf<CappedTireTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedTireTemplateStore(): CappedTireTemplateStore {
    return MemoryCappedTireTemplateStore(mutableMapOf<String, CappedTireTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedTireTemplateStore(
    var inner: CappedTireTemplateStore,
    var cache: TtlCache<CappedTireTemplate>,
    var hits: Int,
    var misses: Int,
) : CappedTireTemplateStore {
    override fun find(code: String): CappedTireTemplate? {
        val hit = (cache.get(code) as CappedTireTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedTireTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedTireTemplate> = inner.list()
}

fun newCachedCappedTireTemplateStore(inner: CappedTireTemplateStore, ttl: Int, clock: Clock): CappedTireTemplateStore {
    return CachedCappedTireTemplateStore(
        inner = inner,
        cache = newTtlCache<CappedTireTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped tire templates. Empty fields match all. */
data class CappedTireTemplateQuery(
    var region: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedTireTemplate): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedTireTemplate>): MutableList<CappedTireTemplate> {
        val matched = filter(items) { candidate: CappedTireTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedTireTemplates(store: CappedTireTemplateStore, query: CappedTireTemplateQuery): MutableList<CappedTireTemplate> {
    return query.apply(store.list())
}
