package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery mappings by code. */
interface ProofOfDeliveryMappingStore {
    fun find(code: String): ProofOfDeliveryMapping?
    fun save(item: ProofOfDeliveryMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryMappingStore(
    var items: MutableMap<String, ProofOfDeliveryMapping>,
    var order: MutableList<String>,
) : ProofOfDeliveryMappingStore {
    override fun find(code: String): ProofOfDeliveryMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryMapping) {
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

    override fun list(): MutableList<ProofOfDeliveryMapping> {
        val out = mutableListOf<ProofOfDeliveryMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryMappingStore(): ProofOfDeliveryMappingStore {
    return MemoryProofOfDeliveryMappingStore(
        mutableMapOf<String, ProofOfDeliveryMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryMappingStore(
    var inner: ProofOfDeliveryMappingStore,
    var cache: TtlCache<ProofOfDeliveryMapping>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryMappingStore {
    override fun find(code: String): ProofOfDeliveryMapping? {
        val hit = (cache.get(code) as ProofOfDeliveryMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryMapping> = inner.list()
}

fun newCachedProofOfDeliveryMappingStore(inner: ProofOfDeliveryMappingStore, ttl: Int, clock: Clock): ProofOfDeliveryMappingStore {
    return CachedProofOfDeliveryMappingStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryMapping>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery mappings. Empty fields match all. */
data class ProofOfDeliveryMappingQuery(
    var locale: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryMapping): Boolean {
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

    fun apply(items: MutableList<ProofOfDeliveryMapping>): MutableList<ProofOfDeliveryMapping> {
        val matched = filter(items) { candidate: ProofOfDeliveryMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryMappings(store: ProofOfDeliveryMappingStore, query: ProofOfDeliveryMappingQuery): MutableList<ProofOfDeliveryMapping> {
    return query.apply(store.list())
}
