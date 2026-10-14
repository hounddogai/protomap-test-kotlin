package com.acme.transport.network.transittime.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time templates by code. */
interface TransitTimeTemplateStore {
    fun find(code: String): TransitTimeTemplate?
    fun save(item: TransitTimeTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeTemplateStore(
    var items: MutableMap<String, TransitTimeTemplate>,
    var order: MutableList<String>,
) : TransitTimeTemplateStore {
    override fun find(code: String): TransitTimeTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeTemplate) {
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

    override fun list(): MutableList<TransitTimeTemplate> {
        val out = mutableListOf<TransitTimeTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeTemplateStore(): TransitTimeTemplateStore {
    return MemoryTransitTimeTemplateStore(mutableMapOf<String, TransitTimeTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeTemplateStore(
    var inner: TransitTimeTemplateStore,
    var cache: TtlCache<TransitTimeTemplate>,
    var hits: Int,
    var misses: Int,
) : TransitTimeTemplateStore {
    override fun find(code: String): TransitTimeTemplate? {
        val hit = (cache.get(code) as TransitTimeTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeTemplate> = inner.list()
}

fun newCachedTransitTimeTemplateStore(inner: TransitTimeTemplateStore, ttl: Int, clock: Clock): TransitTimeTemplateStore {
    return CachedTransitTimeTemplateStore(
        inner = inner,
        cache = newTtlCache<TransitTimeTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time templates. Empty fields match all. */
data class TransitTimeTemplateQuery(
    var region: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeTemplate): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TransitTimeTemplate>): MutableList<TransitTimeTemplate> {
        val matched = filter(items) { candidate: TransitTimeTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeTemplates(store: TransitTimeTemplateStore, query: TransitTimeTemplateQuery): MutableList<TransitTimeTemplate> {
    return query.apply(store.list())
}
