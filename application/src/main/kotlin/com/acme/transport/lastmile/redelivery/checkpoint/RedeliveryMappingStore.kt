package com.acme.transport.lastmile.redelivery.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery mappings by code. */
interface RedeliveryMappingStore {
    fun find(code: String): RedeliveryMapping?
    fun save(item: RedeliveryMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryMappingStore(
    var items: MutableMap<String, RedeliveryMapping>,
    var order: MutableList<String>,
) : RedeliveryMappingStore {
    override fun find(code: String): RedeliveryMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryMapping) {
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

    override fun list(): MutableList<RedeliveryMapping> {
        val out = mutableListOf<RedeliveryMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryMappingStore(): RedeliveryMappingStore {
    return MemoryRedeliveryMappingStore(mutableMapOf<String, RedeliveryMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryMappingStore(
    var inner: RedeliveryMappingStore,
    var cache: TtlCache<RedeliveryMapping>,
    var hits: Int,
    var misses: Int,
) : RedeliveryMappingStore {
    override fun find(code: String): RedeliveryMapping? {
        val hit = (cache.get(code) as RedeliveryMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryMapping> = inner.list()
}

fun newCachedRedeliveryMappingStore(inner: RedeliveryMappingStore, ttl: Int, clock: Clock): RedeliveryMappingStore {
    return CachedRedeliveryMappingStore(
        inner = inner,
        cache = newTtlCache<RedeliveryMapping>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery mappings. Empty fields match all. */
data class RedeliveryMappingQuery(
    var unit: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryMapping): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<RedeliveryMapping>): MutableList<RedeliveryMapping> {
        val matched = filter(items) { candidate: RedeliveryMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryMappings(store: RedeliveryMappingStore, query: RedeliveryMappingQuery): MutableList<RedeliveryMapping> {
    return query.apply(store.list())
}
