package com.acme.transport.customs.broker.metric

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict broker mappings by code. */
interface StrictBrokerMappingStore {
    fun find(code: String): StrictBrokerMapping?
    fun save(item: StrictBrokerMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictBrokerMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictBrokerMappingStore(
    var items: MutableMap<String, StrictBrokerMapping>,
    var order: MutableList<String>,
) : StrictBrokerMappingStore {
    override fun find(code: String): StrictBrokerMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictBrokerMapping) {
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

    override fun list(): MutableList<StrictBrokerMapping> {
        val out = mutableListOf<StrictBrokerMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictBrokerMappingStore(): StrictBrokerMappingStore {
    return MemoryStrictBrokerMappingStore(mutableMapOf<String, StrictBrokerMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictBrokerMappingStore(
    var inner: StrictBrokerMappingStore,
    var cache: TtlCache<StrictBrokerMapping>,
    var hits: Int,
    var misses: Int,
) : StrictBrokerMappingStore {
    override fun find(code: String): StrictBrokerMapping? {
        val hit = (cache.get(code) as StrictBrokerMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictBrokerMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictBrokerMapping> = inner.list()
}

fun newCachedStrictBrokerMappingStore(inner: StrictBrokerMappingStore, ttl: Int, clock: Clock): StrictBrokerMappingStore {
    return CachedStrictBrokerMappingStore(
        inner = inner,
        cache = newTtlCache<StrictBrokerMapping>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict broker mappings. Empty fields match all. */
data class StrictBrokerMappingQuery(
    var segment: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictBrokerMapping): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictBrokerMapping>): MutableList<StrictBrokerMapping> {
        val matched = filter(items) { candidate: StrictBrokerMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictBrokerMappings(store: StrictBrokerMappingStore, query: StrictBrokerMappingQuery): MutableList<StrictBrokerMapping> {
    return query.apply(store.list())
}
