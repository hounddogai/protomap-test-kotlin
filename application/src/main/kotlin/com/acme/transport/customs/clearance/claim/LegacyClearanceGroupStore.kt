package com.acme.transport.customs.clearance.claim

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy clearance groups by code. */
interface LegacyClearanceGroupStore {
    fun find(code: String): LegacyClearanceGroup?
    fun save(item: LegacyClearanceGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyClearanceGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyClearanceGroupStore(
    var items: MutableMap<String, LegacyClearanceGroup>,
    var order: MutableList<String>,
) : LegacyClearanceGroupStore {
    override fun find(code: String): LegacyClearanceGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyClearanceGroup) {
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

    override fun list(): MutableList<LegacyClearanceGroup> {
        val out = mutableListOf<LegacyClearanceGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyClearanceGroupStore(): LegacyClearanceGroupStore {
    return MemoryLegacyClearanceGroupStore(
        mutableMapOf<String, LegacyClearanceGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyClearanceGroupStore(
    var inner: LegacyClearanceGroupStore,
    var cache: TtlCache<LegacyClearanceGroup>,
    var hits: Int,
    var misses: Int,
) : LegacyClearanceGroupStore {
    override fun find(code: String): LegacyClearanceGroup? {
        val hit = (cache.get(code) as LegacyClearanceGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyClearanceGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyClearanceGroup> = inner.list()
}

fun newCachedLegacyClearanceGroupStore(inner: LegacyClearanceGroupStore, ttl: Int, clock: Clock): LegacyClearanceGroupStore {
    return CachedLegacyClearanceGroupStore(
        inner = inner,
        cache = newTtlCache<LegacyClearanceGroup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy clearance groups. Empty fields match all. */
data class LegacyClearanceGroupQuery(
    var lane: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyClearanceGroup): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<LegacyClearanceGroup>): MutableList<LegacyClearanceGroup> {
        val matched = filter(items) { candidate: LegacyClearanceGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyClearanceGroups(store: LegacyClearanceGroupStore, query: LegacyClearanceGroupQuery): MutableList<LegacyClearanceGroup> {
    return query.apply(store.list())
}
