package com.acme.transport.customs.inspection.slot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback inspection overrides by code. */
interface FallbackInspectionOverrideStore {
    fun find(code: String): FallbackInspectionOverride?
    fun save(item: FallbackInspectionOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackInspectionOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackInspectionOverrideStore(
    var items: MutableMap<String, FallbackInspectionOverride>,
    var order: MutableList<String>,
) : FallbackInspectionOverrideStore {
    override fun find(code: String): FallbackInspectionOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackInspectionOverride) {
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

    override fun list(): MutableList<FallbackInspectionOverride> {
        val out = mutableListOf<FallbackInspectionOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackInspectionOverrideStore(): FallbackInspectionOverrideStore {
    return MemoryFallbackInspectionOverrideStore(
        mutableMapOf<String, FallbackInspectionOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackInspectionOverrideStore(
    var inner: FallbackInspectionOverrideStore,
    var cache: TtlCache<FallbackInspectionOverride>,
    var hits: Int,
    var misses: Int,
) : FallbackInspectionOverrideStore {
    override fun find(code: String): FallbackInspectionOverride? {
        val hit = (cache.get(code) as FallbackInspectionOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackInspectionOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackInspectionOverride> = inner.list()
}

fun newCachedFallbackInspectionOverrideStore(inner: FallbackInspectionOverrideStore, ttl: Int, clock: Clock): FallbackInspectionOverrideStore {
    return CachedFallbackInspectionOverrideStore(
        inner = inner,
        cache = newTtlCache<FallbackInspectionOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback inspection overrides. Empty fields match all. */
data class FallbackInspectionOverrideQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackInspectionOverride): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackInspectionOverride>): MutableList<FallbackInspectionOverride> {
        val matched = filter(items) { candidate: FallbackInspectionOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackInspectionOverrides(store: FallbackInspectionOverrideStore, query: FallbackInspectionOverrideQuery): MutableList<FallbackInspectionOverride> {
    return query.apply(store.list())
}
