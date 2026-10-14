package com.acme.transport.safety.nearmiss.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps lenient near miss mappings by code. */
interface LenientNearMissMappingStore {
    fun find(code: String): LenientNearMissMapping?
    fun save(item: LenientNearMissMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientNearMissMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientNearMissMappingStore(
    var items: MutableMap<String, LenientNearMissMapping>,
    var order: MutableList<String>,
) : LenientNearMissMappingStore {
    override fun find(code: String): LenientNearMissMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientNearMissMapping) {
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

    override fun list(): MutableList<LenientNearMissMapping> {
        val out = mutableListOf<LenientNearMissMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientNearMissMappingStore(): LenientNearMissMappingStore {
    return MemoryLenientNearMissMappingStore(
        mutableMapOf<String, LenientNearMissMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientNearMissMappingStore(
    var inner: LenientNearMissMappingStore,
    var cache: TtlCache<LenientNearMissMapping>,
    var hits: Int,
    var misses: Int,
) : LenientNearMissMappingStore {
    override fun find(code: String): LenientNearMissMapping? {
        val hit = (cache.get(code) as LenientNearMissMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientNearMissMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientNearMissMapping> = inner.list()
}

fun newCachedLenientNearMissMappingStore(inner: LenientNearMissMappingStore, ttl: Int, clock: Clock): LenientNearMissMappingStore {
    return CachedLenientNearMissMappingStore(
        inner = inner,
        cache = newTtlCache<LenientNearMissMapping>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient near miss mappings. Empty fields match all. */
data class LenientNearMissMappingQuery(
    var country: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientNearMissMapping): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<LenientNearMissMapping>): MutableList<LenientNearMissMapping> {
        val matched = filter(items) { candidate: LenientNearMissMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientNearMissMappings(store: LenientNearMissMappingStore, query: LenientNearMissMappingQuery): MutableList<LenientNearMissMapping> {
    return query.apply(store.list())
}
