package com.acme.transport.network.hub.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps capped hub mappings by code. */
interface CappedHubMappingStore {
    fun find(code: String): CappedHubMapping?
    fun save(item: CappedHubMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedHubMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedHubMappingStore(
    var items: MutableMap<String, CappedHubMapping>,
    var order: MutableList<String>,
) : CappedHubMappingStore {
    override fun find(code: String): CappedHubMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedHubMapping) {
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

    override fun list(): MutableList<CappedHubMapping> {
        val out = mutableListOf<CappedHubMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedHubMappingStore(): CappedHubMappingStore {
    return MemoryCappedHubMappingStore(mutableMapOf<String, CappedHubMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedHubMappingStore(
    var inner: CappedHubMappingStore,
    var cache: TtlCache<CappedHubMapping>,
    var hits: Int,
    var misses: Int,
) : CappedHubMappingStore {
    override fun find(code: String): CappedHubMapping? {
        val hit = (cache.get(code) as CappedHubMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedHubMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedHubMapping> = inner.list()
}

fun newCachedCappedHubMappingStore(inner: CappedHubMappingStore, ttl: Int, clock: Clock): CappedHubMappingStore {
    return CachedCappedHubMappingStore(
        inner = inner,
        cache = newTtlCache<CappedHubMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped hub mappings. Empty fields match all. */
data class CappedHubMappingQuery(
    var country: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedHubMapping): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedHubMapping>): MutableList<CappedHubMapping> {
        val matched = filter(items) { candidate: CappedHubMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedHubMappings(store: CappedHubMappingStore, query: CappedHubMappingQuery): MutableList<CappedHubMapping> {
    return query.apply(store.list())
}
