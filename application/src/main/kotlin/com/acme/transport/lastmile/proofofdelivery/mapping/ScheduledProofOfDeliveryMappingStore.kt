package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled proof of delivery mappings by code. */
interface ScheduledProofOfDeliveryMappingStore {
    fun find(code: String): ScheduledProofOfDeliveryMapping?
    fun save(item: ScheduledProofOfDeliveryMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledProofOfDeliveryMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledProofOfDeliveryMappingStore(
    var items: MutableMap<String, ScheduledProofOfDeliveryMapping>,
    var order: MutableList<String>,
) : ScheduledProofOfDeliveryMappingStore {
    override fun find(code: String): ScheduledProofOfDeliveryMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledProofOfDeliveryMapping) {
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

    override fun list(): MutableList<ScheduledProofOfDeliveryMapping> {
        val out = mutableListOf<ScheduledProofOfDeliveryMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledProofOfDeliveryMappingStore(): ScheduledProofOfDeliveryMappingStore {
    return MemoryScheduledProofOfDeliveryMappingStore(
        mutableMapOf<String, ScheduledProofOfDeliveryMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledProofOfDeliveryMappingStore(
    var inner: ScheduledProofOfDeliveryMappingStore,
    var cache: TtlCache<ScheduledProofOfDeliveryMapping>,
    var hits: Int,
    var misses: Int,
) : ScheduledProofOfDeliveryMappingStore {
    override fun find(code: String): ScheduledProofOfDeliveryMapping? {
        val hit = (cache.get(code) as ScheduledProofOfDeliveryMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledProofOfDeliveryMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledProofOfDeliveryMapping> = inner.list()
}

fun newCachedScheduledProofOfDeliveryMappingStore(inner: ScheduledProofOfDeliveryMappingStore, ttl: Int, clock: Clock): ScheduledProofOfDeliveryMappingStore {
    return CachedScheduledProofOfDeliveryMappingStore(
        inner = inner,
        cache = newTtlCache<ScheduledProofOfDeliveryMapping>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled proof of delivery mappings. Empty fields match all. */
data class ScheduledProofOfDeliveryMappingQuery(
    var country: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledProofOfDeliveryMapping): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledProofOfDeliveryMapping>): MutableList<ScheduledProofOfDeliveryMapping> {
        val matched = filter(items) { candidate: ScheduledProofOfDeliveryMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledProofOfDeliveryMappings(store: ScheduledProofOfDeliveryMappingStore, query: ScheduledProofOfDeliveryMappingQuery): MutableList<ScheduledProofOfDeliveryMapping> {
    return query.apply(store.list())
}
