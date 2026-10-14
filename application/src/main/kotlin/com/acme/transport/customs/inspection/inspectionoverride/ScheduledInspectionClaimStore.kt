package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled inspection claims by code. */
interface ScheduledInspectionClaimStore {
    fun find(code: String): ScheduledInspectionClaim?
    fun save(item: ScheduledInspectionClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledInspectionClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledInspectionClaimStore(
    var items: MutableMap<String, ScheduledInspectionClaim>,
    var order: MutableList<String>,
) : ScheduledInspectionClaimStore {
    override fun find(code: String): ScheduledInspectionClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledInspectionClaim) {
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

    override fun list(): MutableList<ScheduledInspectionClaim> {
        val out = mutableListOf<ScheduledInspectionClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledInspectionClaimStore(): ScheduledInspectionClaimStore {
    return MemoryScheduledInspectionClaimStore(
        mutableMapOf<String, ScheduledInspectionClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledInspectionClaimStore(
    var inner: ScheduledInspectionClaimStore,
    var cache: TtlCache<ScheduledInspectionClaim>,
    var hits: Int,
    var misses: Int,
) : ScheduledInspectionClaimStore {
    override fun find(code: String): ScheduledInspectionClaim? {
        val hit = (cache.get(code) as ScheduledInspectionClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledInspectionClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledInspectionClaim> = inner.list()
}

fun newCachedScheduledInspectionClaimStore(inner: ScheduledInspectionClaimStore, ttl: Int, clock: Clock): ScheduledInspectionClaimStore {
    return CachedScheduledInspectionClaimStore(
        inner = inner,
        cache = newTtlCache<ScheduledInspectionClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled inspection claims. Empty fields match all. */
data class ScheduledInspectionClaimQuery(
    var lane: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledInspectionClaim): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledInspectionClaim>): MutableList<ScheduledInspectionClaim> {
        val matched = filter(items) { candidate: ScheduledInspectionClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledInspectionClaims(store: ScheduledInspectionClaimStore, query: ScheduledInspectionClaimQuery): MutableList<ScheduledInspectionClaim> {
    return query.apply(store.list())
}
