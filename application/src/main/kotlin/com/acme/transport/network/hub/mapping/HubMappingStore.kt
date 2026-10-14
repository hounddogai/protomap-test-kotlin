package com.acme.transport.network.hub.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub mappings by code. */
interface HubMappingStore {
    fun find(code: String): HubMapping?
    fun save(item: HubMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubMappingStore(
    var items: MutableMap<String, HubMapping>,
    var order: MutableList<String>,
) : HubMappingStore {
    override fun find(code: String): HubMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubMapping) {
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

    override fun list(): MutableList<HubMapping> {
        val out = mutableListOf<HubMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubMappingStore(): HubMappingStore {
    return MemoryHubMappingStore(mutableMapOf<String, HubMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubMappingStore(
    var inner: HubMappingStore,
    var cache: TtlCache<HubMapping>,
    var hits: Int,
    var misses: Int,
) : HubMappingStore {
    override fun find(code: String): HubMapping? {
        val hit = (cache.get(code) as HubMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubMapping> = inner.list()
}

fun newCachedHubMappingStore(inner: HubMappingStore, ttl: Int, clock: Clock): HubMappingStore {
    return CachedHubMappingStore(
        inner = inner,
        cache = newTtlCache<HubMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub mappings. Empty fields match all. */
data class HubMappingQuery(
    var locale: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubMapping): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubMapping>): MutableList<HubMapping> {
        val matched = filter(items) { candidate: HubMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubMappings(store: HubMappingStore, query: HubMappingQuery): MutableList<HubMapping> {
    return query.apply(store.list())
}
