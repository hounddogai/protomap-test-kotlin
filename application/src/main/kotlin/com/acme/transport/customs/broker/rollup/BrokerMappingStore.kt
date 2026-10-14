package com.acme.transport.customs.broker.rollup

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker mappings by code. */
interface BrokerMappingStore {
    fun find(code: String): BrokerMapping?
    fun save(item: BrokerMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerMappingStore(
    var items: MutableMap<String, BrokerMapping>,
    var order: MutableList<String>,
) : BrokerMappingStore {
    override fun find(code: String): BrokerMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerMapping) {
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

    override fun list(): MutableList<BrokerMapping> {
        val out = mutableListOf<BrokerMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerMappingStore(): BrokerMappingStore {
    return MemoryBrokerMappingStore(mutableMapOf<String, BrokerMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerMappingStore(
    var inner: BrokerMappingStore,
    var cache: TtlCache<BrokerMapping>,
    var hits: Int,
    var misses: Int,
) : BrokerMappingStore {
    override fun find(code: String): BrokerMapping? {
        val hit = (cache.get(code) as BrokerMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerMapping> = inner.list()
}

fun newCachedBrokerMappingStore(inner: BrokerMappingStore, ttl: Int, clock: Clock): BrokerMappingStore {
    return CachedBrokerMappingStore(
        inner = inner,
        cache = newTtlCache<BrokerMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker mappings. Empty fields match all. */
data class BrokerMappingQuery(
    var title: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerMapping): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerMapping>): MutableList<BrokerMapping> {
        val matched = filter(items) { candidate: BrokerMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerMappings(store: BrokerMappingStore, query: BrokerMappingQuery): MutableList<BrokerMapping> {
    return query.apply(store.list())
}
