package com.acme.transport.customs.declaration.segment

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual declaration adjustments by code. */
interface ManualDeclarationAdjustmentStore {
    fun find(code: String): ManualDeclarationAdjustment?
    fun save(item: ManualDeclarationAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualDeclarationAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualDeclarationAdjustmentStore(
    var items: MutableMap<String, ManualDeclarationAdjustment>,
    var order: MutableList<String>,
) : ManualDeclarationAdjustmentStore {
    override fun find(code: String): ManualDeclarationAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualDeclarationAdjustment) {
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

    override fun list(): MutableList<ManualDeclarationAdjustment> {
        val out = mutableListOf<ManualDeclarationAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualDeclarationAdjustmentStore(): ManualDeclarationAdjustmentStore {
    return MemoryManualDeclarationAdjustmentStore(
        mutableMapOf<String, ManualDeclarationAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualDeclarationAdjustmentStore(
    var inner: ManualDeclarationAdjustmentStore,
    var cache: TtlCache<ManualDeclarationAdjustment>,
    var hits: Int,
    var misses: Int,
) : ManualDeclarationAdjustmentStore {
    override fun find(code: String): ManualDeclarationAdjustment? {
        val hit = (cache.get(code) as ManualDeclarationAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualDeclarationAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualDeclarationAdjustment> = inner.list()
}

fun newCachedManualDeclarationAdjustmentStore(inner: ManualDeclarationAdjustmentStore, ttl: Int, clock: Clock): ManualDeclarationAdjustmentStore {
    return CachedManualDeclarationAdjustmentStore(
        inner = inner,
        cache = newTtlCache<ManualDeclarationAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual declaration adjustments. Empty fields match all. */
data class ManualDeclarationAdjustmentQuery(
    var locale: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualDeclarationAdjustment): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<ManualDeclarationAdjustment>): MutableList<ManualDeclarationAdjustment> {
        val matched = filter(items) { candidate: ManualDeclarationAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualDeclarationAdjustments(store: ManualDeclarationAdjustmentStore, query: ManualDeclarationAdjustmentQuery): MutableList<ManualDeclarationAdjustment> {
    return query.apply(store.list())
}
