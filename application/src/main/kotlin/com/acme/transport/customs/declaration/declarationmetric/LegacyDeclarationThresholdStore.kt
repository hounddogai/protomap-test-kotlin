package com.acme.transport.customs.declaration.declarationmetric

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy declaration thresholds by code. */
interface LegacyDeclarationThresholdStore {
    fun find(code: String): LegacyDeclarationThreshold?
    fun save(item: LegacyDeclarationThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyDeclarationThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyDeclarationThresholdStore(
    var items: MutableMap<String, LegacyDeclarationThreshold>,
    var order: MutableList<String>,
) : LegacyDeclarationThresholdStore {
    override fun find(code: String): LegacyDeclarationThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyDeclarationThreshold) {
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

    override fun list(): MutableList<LegacyDeclarationThreshold> {
        val out = mutableListOf<LegacyDeclarationThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyDeclarationThresholdStore(): LegacyDeclarationThresholdStore {
    return MemoryLegacyDeclarationThresholdStore(
        mutableMapOf<String, LegacyDeclarationThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyDeclarationThresholdStore(
    var inner: LegacyDeclarationThresholdStore,
    var cache: TtlCache<LegacyDeclarationThreshold>,
    var hits: Int,
    var misses: Int,
) : LegacyDeclarationThresholdStore {
    override fun find(code: String): LegacyDeclarationThreshold? {
        val hit = (cache.get(code) as LegacyDeclarationThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyDeclarationThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyDeclarationThreshold> = inner.list()
}

fun newCachedLegacyDeclarationThresholdStore(inner: LegacyDeclarationThresholdStore, ttl: Int, clock: Clock): LegacyDeclarationThresholdStore {
    return CachedLegacyDeclarationThresholdStore(
        inner = inner,
        cache = newTtlCache<LegacyDeclarationThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy declaration thresholds. Empty fields match all. */
data class LegacyDeclarationThresholdQuery(
    var unit: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyDeclarationThreshold): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyDeclarationThreshold>): MutableList<LegacyDeclarationThreshold> {
        val matched = filter(items) { candidate: LegacyDeclarationThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyDeclarationThresholds(store: LegacyDeclarationThresholdStore, query: LegacyDeclarationThresholdQuery): MutableList<LegacyDeclarationThreshold> {
    return query.apply(store.list())
}
