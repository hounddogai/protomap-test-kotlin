package com.acme.transport.lastmile.parcel.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps lenient parcel mappings by code. */
interface LenientParcelMappingStore {
    fun find(code: String): LenientParcelMapping?
    fun save(item: LenientParcelMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientParcelMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientParcelMappingStore(
    var items: MutableMap<String, LenientParcelMapping>,
    var order: MutableList<String>,
) : LenientParcelMappingStore {
    override fun find(code: String): LenientParcelMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientParcelMapping) {
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

    override fun list(): MutableList<LenientParcelMapping> {
        val out = mutableListOf<LenientParcelMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientParcelMappingStore(): LenientParcelMappingStore {
    return MemoryLenientParcelMappingStore(
        mutableMapOf<String, LenientParcelMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientParcelMappingStore(
    var inner: LenientParcelMappingStore,
    var cache: TtlCache<LenientParcelMapping>,
    var hits: Int,
    var misses: Int,
) : LenientParcelMappingStore {
    override fun find(code: String): LenientParcelMapping? {
        val hit = (cache.get(code) as LenientParcelMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientParcelMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientParcelMapping> = inner.list()
}

fun newCachedLenientParcelMappingStore(inner: LenientParcelMappingStore, ttl: Int, clock: Clock): LenientParcelMappingStore {
    return CachedLenientParcelMappingStore(
        inner = inner,
        cache = newTtlCache<LenientParcelMapping>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient parcel mappings. Empty fields match all. */
data class LenientParcelMappingQuery(
    var category: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientParcelMapping): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientParcelMapping>): MutableList<LenientParcelMapping> {
        val matched = filter(items) { candidate: LenientParcelMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientParcelMappings(store: LenientParcelMappingStore, query: LenientParcelMappingQuery): MutableList<LenientParcelMapping> {
    return query.apply(store.list())
}
