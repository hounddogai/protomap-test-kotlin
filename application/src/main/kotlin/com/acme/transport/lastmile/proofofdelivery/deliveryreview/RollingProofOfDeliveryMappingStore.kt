package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps rolling proof of delivery mappings by code. */
interface RollingProofOfDeliveryMappingStore {
    fun find(code: String): RollingProofOfDeliveryMapping?
    fun save(item: RollingProofOfDeliveryMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingProofOfDeliveryMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingProofOfDeliveryMappingStore(
    var items: MutableMap<String, RollingProofOfDeliveryMapping>,
    var order: MutableList<String>,
) : RollingProofOfDeliveryMappingStore {
    override fun find(code: String): RollingProofOfDeliveryMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingProofOfDeliveryMapping) {
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

    override fun list(): MutableList<RollingProofOfDeliveryMapping> {
        val out = mutableListOf<RollingProofOfDeliveryMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingProofOfDeliveryMappingStore(): RollingProofOfDeliveryMappingStore {
    return MemoryRollingProofOfDeliveryMappingStore(
        mutableMapOf<String, RollingProofOfDeliveryMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingProofOfDeliveryMappingStore(
    var inner: RollingProofOfDeliveryMappingStore,
    var cache: TtlCache<RollingProofOfDeliveryMapping>,
    var hits: Int,
    var misses: Int,
) : RollingProofOfDeliveryMappingStore {
    override fun find(code: String): RollingProofOfDeliveryMapping? {
        val hit = (cache.get(code) as RollingProofOfDeliveryMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingProofOfDeliveryMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingProofOfDeliveryMapping> = inner.list()
}

fun newCachedRollingProofOfDeliveryMappingStore(inner: RollingProofOfDeliveryMappingStore, ttl: Int, clock: Clock): RollingProofOfDeliveryMappingStore {
    return CachedRollingProofOfDeliveryMappingStore(
        inner = inner,
        cache = newTtlCache<RollingProofOfDeliveryMapping>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling proof of delivery mappings. Empty fields match all. */
data class RollingProofOfDeliveryMappingQuery(
    var depot: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingProofOfDeliveryMapping): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingProofOfDeliveryMapping>): MutableList<RollingProofOfDeliveryMapping> {
        val matched = filter(items) { candidate: RollingProofOfDeliveryMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingProofOfDeliveryMappings(store: RollingProofOfDeliveryMappingStore, query: RollingProofOfDeliveryMappingQuery): MutableList<RollingProofOfDeliveryMapping> {
    return query.apply(store.list())
}
