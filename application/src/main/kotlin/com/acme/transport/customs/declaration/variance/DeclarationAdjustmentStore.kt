package com.acme.transport.customs.declaration.variance

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration adjustments by code. */
interface DeclarationAdjustmentStore {
    fun find(code: String): DeclarationAdjustment?
    fun save(item: DeclarationAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationAdjustmentStore(
    var items: MutableMap<String, DeclarationAdjustment>,
    var order: MutableList<String>,
) : DeclarationAdjustmentStore {
    override fun find(code: String): DeclarationAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationAdjustment) {
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

    override fun list(): MutableList<DeclarationAdjustment> {
        val out = mutableListOf<DeclarationAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationAdjustmentStore(): DeclarationAdjustmentStore {
    return MemoryDeclarationAdjustmentStore(
        mutableMapOf<String, DeclarationAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationAdjustmentStore(
    var inner: DeclarationAdjustmentStore,
    var cache: TtlCache<DeclarationAdjustment>,
    var hits: Int,
    var misses: Int,
) : DeclarationAdjustmentStore {
    override fun find(code: String): DeclarationAdjustment? {
        val hit = (cache.get(code) as DeclarationAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationAdjustment> = inner.list()
}

fun newCachedDeclarationAdjustmentStore(inner: DeclarationAdjustmentStore, ttl: Int, clock: Clock): DeclarationAdjustmentStore {
    return CachedDeclarationAdjustmentStore(
        inner = inner,
        cache = newTtlCache<DeclarationAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration adjustments. Empty fields match all. */
data class DeclarationAdjustmentQuery(
    var label: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationAdjustment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationAdjustment>): MutableList<DeclarationAdjustment> {
        val matched = filter(items) { candidate: DeclarationAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationAdjustments(store: DeclarationAdjustmentStore, query: DeclarationAdjustmentQuery): MutableList<DeclarationAdjustment> {
    return query.apply(store.list())
}
