package com.acme.transport.freightpay.accessorial.overrideitem

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial mappings by code. */
interface AccessorialMappingStore {
    fun find(code: String): AccessorialMapping?
    fun save(item: AccessorialMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialMappingStore(
    var items: MutableMap<String, AccessorialMapping>,
    var order: MutableList<String>,
) : AccessorialMappingStore {
    override fun find(code: String): AccessorialMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialMapping) {
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

    override fun list(): MutableList<AccessorialMapping> {
        val out = mutableListOf<AccessorialMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialMappingStore(): AccessorialMappingStore {
    return MemoryAccessorialMappingStore(mutableMapOf<String, AccessorialMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialMappingStore(
    var inner: AccessorialMappingStore,
    var cache: TtlCache<AccessorialMapping>,
    var hits: Int,
    var misses: Int,
) : AccessorialMappingStore {
    override fun find(code: String): AccessorialMapping? {
        val hit = (cache.get(code) as AccessorialMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialMapping> = inner.list()
}

fun newCachedAccessorialMappingStore(inner: AccessorialMappingStore, ttl: Int, clock: Clock): AccessorialMappingStore {
    return CachedAccessorialMappingStore(
        inner = inner,
        cache = newTtlCache<AccessorialMapping>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial mappings. Empty fields match all. */
data class AccessorialMappingQuery(
    var country: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialMapping): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AccessorialMapping>): MutableList<AccessorialMapping> {
        val matched = filter(items) { candidate: AccessorialMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialMappings(store: AccessorialMappingStore, query: AccessorialMappingQuery): MutableList<AccessorialMapping> {
    return query.apply(store.list())
}
