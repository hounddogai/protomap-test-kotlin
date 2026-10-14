package com.acme.transport.linehaul.relay.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay mappings by code. */
interface RelayMappingStore {
    fun find(code: String): RelayMapping?
    fun save(item: RelayMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayMappingStore(
    var items: MutableMap<String, RelayMapping>,
    var order: MutableList<String>,
) : RelayMappingStore {
    override fun find(code: String): RelayMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayMapping) {
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

    override fun list(): MutableList<RelayMapping> {
        val out = mutableListOf<RelayMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayMappingStore(): RelayMappingStore {
    return MemoryRelayMappingStore(mutableMapOf<String, RelayMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayMappingStore(
    var inner: RelayMappingStore,
    var cache: TtlCache<RelayMapping>,
    var hits: Int,
    var misses: Int,
) : RelayMappingStore {
    override fun find(code: String): RelayMapping? {
        val hit = (cache.get(code) as RelayMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayMapping> = inner.list()
}

fun newCachedRelayMappingStore(inner: RelayMappingStore, ttl: Int, clock: Clock): RelayMappingStore {
    return CachedRelayMappingStore(
        inner = inner,
        cache = newTtlCache<RelayMapping>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay mappings. Empty fields match all. */
data class RelayMappingQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayMapping): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayMapping>): MutableList<RelayMapping> {
        val matched = filter(items) { candidate: RelayMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayMappings(store: RelayMappingStore, query: RelayMappingQuery): MutableList<RelayMapping> {
    return query.apply(store.list())
}
