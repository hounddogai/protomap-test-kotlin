package com.acme.transport.linehaul.relay.relaymapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps primary relay mappings by code. */
interface PrimaryRelayMappingStore {
    fun find(code: String): PrimaryRelayMapping?
    fun save(item: PrimaryRelayMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryRelayMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryRelayMappingStore(
    var items: MutableMap<String, PrimaryRelayMapping>,
    var order: MutableList<String>,
) : PrimaryRelayMappingStore {
    override fun find(code: String): PrimaryRelayMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryRelayMapping) {
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

    override fun list(): MutableList<PrimaryRelayMapping> {
        val out = mutableListOf<PrimaryRelayMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryRelayMappingStore(): PrimaryRelayMappingStore {
    return MemoryPrimaryRelayMappingStore(mutableMapOf<String, PrimaryRelayMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryRelayMappingStore(
    var inner: PrimaryRelayMappingStore,
    var cache: TtlCache<PrimaryRelayMapping>,
    var hits: Int,
    var misses: Int,
) : PrimaryRelayMappingStore {
    override fun find(code: String): PrimaryRelayMapping? {
        val hit = (cache.get(code) as PrimaryRelayMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryRelayMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryRelayMapping> = inner.list()
}

fun newCachedPrimaryRelayMappingStore(inner: PrimaryRelayMappingStore, ttl: Int, clock: Clock): PrimaryRelayMappingStore {
    return CachedPrimaryRelayMappingStore(
        inner = inner,
        cache = newTtlCache<PrimaryRelayMapping>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary relay mappings. Empty fields match all. */
data class PrimaryRelayMappingQuery(
    var locale: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryRelayMapping): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<PrimaryRelayMapping>): MutableList<PrimaryRelayMapping> {
        val matched = filter(items) { candidate: PrimaryRelayMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryRelayMappings(store: PrimaryRelayMappingStore, query: PrimaryRelayMappingQuery): MutableList<PrimaryRelayMapping> {
    return query.apply(store.list())
}
