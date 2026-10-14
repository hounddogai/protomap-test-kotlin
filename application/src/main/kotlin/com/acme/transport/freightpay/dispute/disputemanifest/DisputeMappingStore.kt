package com.acme.transport.freightpay.dispute.disputemanifest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute mappings by code. */
interface DisputeMappingStore {
    fun find(code: String): DisputeMapping?
    fun save(item: DisputeMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeMappingStore(
    var items: MutableMap<String, DisputeMapping>,
    var order: MutableList<String>,
) : DisputeMappingStore {
    override fun find(code: String): DisputeMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeMapping) {
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

    override fun list(): MutableList<DisputeMapping> {
        val out = mutableListOf<DisputeMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeMappingStore(): DisputeMappingStore {
    return MemoryDisputeMappingStore(mutableMapOf<String, DisputeMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeMappingStore(
    var inner: DisputeMappingStore,
    var cache: TtlCache<DisputeMapping>,
    var hits: Int,
    var misses: Int,
) : DisputeMappingStore {
    override fun find(code: String): DisputeMapping? {
        val hit = (cache.get(code) as DisputeMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeMapping> = inner.list()
}

fun newCachedDisputeMappingStore(inner: DisputeMappingStore, ttl: Int, clock: Clock): DisputeMappingStore {
    return CachedDisputeMappingStore(
        inner = inner,
        cache = newTtlCache<DisputeMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute mappings. Empty fields match all. */
data class DisputeMappingQuery(
    var segment: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeMapping): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<DisputeMapping>): MutableList<DisputeMapping> {
        val matched = filter(items) { candidate: DisputeMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeMappings(store: DisputeMappingStore, query: DisputeMappingQuery): MutableList<DisputeMapping> {
    return query.apply(store.list())
}
