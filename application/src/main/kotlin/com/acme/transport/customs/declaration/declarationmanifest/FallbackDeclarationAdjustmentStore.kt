package com.acme.transport.customs.declaration.declarationmanifest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback declaration adjustments by code. */
interface FallbackDeclarationAdjustmentStore {
    fun find(code: String): FallbackDeclarationAdjustment?
    fun save(item: FallbackDeclarationAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDeclarationAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDeclarationAdjustmentStore(
    var items: MutableMap<String, FallbackDeclarationAdjustment>,
    var order: MutableList<String>,
) : FallbackDeclarationAdjustmentStore {
    override fun find(code: String): FallbackDeclarationAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDeclarationAdjustment) {
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

    override fun list(): MutableList<FallbackDeclarationAdjustment> {
        val out = mutableListOf<FallbackDeclarationAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDeclarationAdjustmentStore(): FallbackDeclarationAdjustmentStore {
    return MemoryFallbackDeclarationAdjustmentStore(
        mutableMapOf<String, FallbackDeclarationAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDeclarationAdjustmentStore(
    var inner: FallbackDeclarationAdjustmentStore,
    var cache: TtlCache<FallbackDeclarationAdjustment>,
    var hits: Int,
    var misses: Int,
) : FallbackDeclarationAdjustmentStore {
    override fun find(code: String): FallbackDeclarationAdjustment? {
        val hit = (cache.get(code) as FallbackDeclarationAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDeclarationAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDeclarationAdjustment> = inner.list()
}

fun newCachedFallbackDeclarationAdjustmentStore(inner: FallbackDeclarationAdjustmentStore, ttl: Int, clock: Clock): FallbackDeclarationAdjustmentStore {
    return CachedFallbackDeclarationAdjustmentStore(
        inner = inner,
        cache = newTtlCache<FallbackDeclarationAdjustment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback declaration adjustments. Empty fields match all. */
data class FallbackDeclarationAdjustmentQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDeclarationAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDeclarationAdjustment>): MutableList<FallbackDeclarationAdjustment> {
        val matched = filter(items) { candidate: FallbackDeclarationAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDeclarationAdjustments(store: FallbackDeclarationAdjustmentStore, query: FallbackDeclarationAdjustmentQuery): MutableList<FallbackDeclarationAdjustment> {
    return query.apply(store.list())
}
