package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender mappings by code. */
interface TenderMappingStore {
    fun find(code: String): TenderMapping?
    fun save(item: TenderMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderMappingStore(
    var items: MutableMap<String, TenderMapping>,
    var order: MutableList<String>,
) : TenderMappingStore {
    override fun find(code: String): TenderMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderMapping) {
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

    override fun list(): MutableList<TenderMapping> {
        val out = mutableListOf<TenderMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderMappingStore(): TenderMappingStore {
    return MemoryTenderMappingStore(mutableMapOf<String, TenderMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderMappingStore(
    var inner: TenderMappingStore,
    var cache: TtlCache<TenderMapping>,
    var hits: Int,
    var misses: Int,
) : TenderMappingStore {
    override fun find(code: String): TenderMapping? {
        val hit = (cache.get(code) as TenderMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderMapping> = inner.list()
}

fun newCachedTenderMappingStore(inner: TenderMappingStore, ttl: Int, clock: Clock): TenderMappingStore {
    return CachedTenderMappingStore(
        inner = inner,
        cache = newTtlCache<TenderMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender mappings. Empty fields match all. */
data class TenderMappingQuery(
    var lane: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderMapping): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TenderMapping>): MutableList<TenderMapping> {
        val matched = filter(items) { candidate: TenderMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderMappings(store: TenderMappingStore, query: TenderMappingQuery): MutableList<TenderMapping> {
    return query.apply(store.list())
}
