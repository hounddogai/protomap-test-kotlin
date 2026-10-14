package com.acme.transport.fleetops.inspection.estimate

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy inspection claims by code. */
interface LegacyInspectionClaimStore {
    fun find(code: String): LegacyInspectionClaim?
    fun save(item: LegacyInspectionClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyInspectionClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyInspectionClaimStore(
    var items: MutableMap<String, LegacyInspectionClaim>,
    var order: MutableList<String>,
) : LegacyInspectionClaimStore {
    override fun find(code: String): LegacyInspectionClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyInspectionClaim) {
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

    override fun list(): MutableList<LegacyInspectionClaim> {
        val out = mutableListOf<LegacyInspectionClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyInspectionClaimStore(): LegacyInspectionClaimStore {
    return MemoryLegacyInspectionClaimStore(
        mutableMapOf<String, LegacyInspectionClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyInspectionClaimStore(
    var inner: LegacyInspectionClaimStore,
    var cache: TtlCache<LegacyInspectionClaim>,
    var hits: Int,
    var misses: Int,
) : LegacyInspectionClaimStore {
    override fun find(code: String): LegacyInspectionClaim? {
        val hit = (cache.get(code) as LegacyInspectionClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyInspectionClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyInspectionClaim> = inner.list()
}

fun newCachedLegacyInspectionClaimStore(inner: LegacyInspectionClaimStore, ttl: Int, clock: Clock): LegacyInspectionClaimStore {
    return CachedLegacyInspectionClaimStore(
        inner = inner,
        cache = newTtlCache<LegacyInspectionClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy inspection claims. Empty fields match all. */
data class LegacyInspectionClaimQuery(
    var title: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyInspectionClaim): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyInspectionClaim>): MutableList<LegacyInspectionClaim> {
        val matched = filter(items) { candidate: LegacyInspectionClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyInspectionClaims(store: LegacyInspectionClaimStore, query: LegacyInspectionClaimQuery): MutableList<LegacyInspectionClaim> {
    return query.apply(store.list())
}
