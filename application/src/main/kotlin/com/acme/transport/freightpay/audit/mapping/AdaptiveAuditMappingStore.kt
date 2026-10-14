package com.acme.transport.freightpay.audit.mapping

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive audit mappings by code. */
interface AdaptiveAuditMappingStore {
    fun find(code: String): AdaptiveAuditMapping?
    fun save(item: AdaptiveAuditMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveAuditMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveAuditMappingStore(
    var items: MutableMap<String, AdaptiveAuditMapping>,
    var order: MutableList<String>,
) : AdaptiveAuditMappingStore {
    override fun find(code: String): AdaptiveAuditMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveAuditMapping) {
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

    override fun list(): MutableList<AdaptiveAuditMapping> {
        val out = mutableListOf<AdaptiveAuditMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveAuditMappingStore(): AdaptiveAuditMappingStore {
    return MemoryAdaptiveAuditMappingStore(
        mutableMapOf<String, AdaptiveAuditMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveAuditMappingStore(
    var inner: AdaptiveAuditMappingStore,
    var cache: TtlCache<AdaptiveAuditMapping>,
    var hits: Int,
    var misses: Int,
) : AdaptiveAuditMappingStore {
    override fun find(code: String): AdaptiveAuditMapping? {
        val hit = (cache.get(code) as AdaptiveAuditMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveAuditMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveAuditMapping> = inner.list()
}

fun newCachedAdaptiveAuditMappingStore(inner: AdaptiveAuditMappingStore, ttl: Int, clock: Clock): AdaptiveAuditMappingStore {
    return CachedAdaptiveAuditMappingStore(
        inner = inner,
        cache = newTtlCache<AdaptiveAuditMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive audit mappings. Empty fields match all. */
data class AdaptiveAuditMappingQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveAuditMapping): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveAuditMapping>): MutableList<AdaptiveAuditMapping> {
        val matched = filter(items) { candidate: AdaptiveAuditMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveAuditMappings(store: AdaptiveAuditMappingStore, query: AdaptiveAuditMappingQuery): MutableList<AdaptiveAuditMapping> {
    return query.apply(store.list())
}
