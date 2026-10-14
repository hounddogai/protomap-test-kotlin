package com.acme.transport.customs.broker.checkpoint

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped broker mappings by code. */
interface CappedBrokerMappingStore {
    fun find(code: String): CappedBrokerMapping?
    fun save(item: CappedBrokerMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedBrokerMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedBrokerMappingStore(
    var items: MutableMap<String, CappedBrokerMapping>,
    var order: MutableList<String>,
) : CappedBrokerMappingStore {
    override fun find(code: String): CappedBrokerMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedBrokerMapping) {
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

    override fun list(): MutableList<CappedBrokerMapping> {
        val out = mutableListOf<CappedBrokerMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedBrokerMappingStore(): CappedBrokerMappingStore {
    return MemoryCappedBrokerMappingStore(mutableMapOf<String, CappedBrokerMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedBrokerMappingStore(
    var inner: CappedBrokerMappingStore,
    var cache: TtlCache<CappedBrokerMapping>,
    var hits: Int,
    var misses: Int,
) : CappedBrokerMappingStore {
    override fun find(code: String): CappedBrokerMapping? {
        val hit = (cache.get(code) as CappedBrokerMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedBrokerMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedBrokerMapping> = inner.list()
}

fun newCachedCappedBrokerMappingStore(inner: CappedBrokerMappingStore, ttl: Int, clock: Clock): CappedBrokerMappingStore {
    return CachedCappedBrokerMappingStore(
        inner = inner,
        cache = newTtlCache<CappedBrokerMapping>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped broker mappings. Empty fields match all. */
data class CappedBrokerMappingQuery(
    var label: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedBrokerMapping): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<CappedBrokerMapping>): MutableList<CappedBrokerMapping> {
        val matched = filter(items) { candidate: CappedBrokerMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedBrokerMappings(store: CappedBrokerMappingStore, query: CappedBrokerMappingQuery): MutableList<CappedBrokerMapping> {
    return query.apply(store.list())
}
