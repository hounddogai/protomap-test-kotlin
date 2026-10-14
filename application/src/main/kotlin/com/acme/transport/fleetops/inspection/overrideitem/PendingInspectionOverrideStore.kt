package com.acme.transport.fleetops.inspection.overrideitem

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending inspection overrides by code. */
interface PendingInspectionOverrideStore {
    fun find(code: String): PendingInspectionOverride?
    fun save(item: PendingInspectionOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingInspectionOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingInspectionOverrideStore(
    var items: MutableMap<String, PendingInspectionOverride>,
    var order: MutableList<String>,
) : PendingInspectionOverrideStore {
    override fun find(code: String): PendingInspectionOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingInspectionOverride) {
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

    override fun list(): MutableList<PendingInspectionOverride> {
        val out = mutableListOf<PendingInspectionOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingInspectionOverrideStore(): PendingInspectionOverrideStore {
    return MemoryPendingInspectionOverrideStore(
        mutableMapOf<String, PendingInspectionOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingInspectionOverrideStore(
    var inner: PendingInspectionOverrideStore,
    var cache: TtlCache<PendingInspectionOverride>,
    var hits: Int,
    var misses: Int,
) : PendingInspectionOverrideStore {
    override fun find(code: String): PendingInspectionOverride? {
        val hit = (cache.get(code) as PendingInspectionOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingInspectionOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingInspectionOverride> = inner.list()
}

fun newCachedPendingInspectionOverrideStore(inner: PendingInspectionOverrideStore, ttl: Int, clock: Clock): PendingInspectionOverrideStore {
    return CachedPendingInspectionOverrideStore(
        inner = inner,
        cache = newTtlCache<PendingInspectionOverride>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending inspection overrides. Empty fields match all. */
data class PendingInspectionOverrideQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingInspectionOverride): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingInspectionOverride>): MutableList<PendingInspectionOverride> {
        val matched = filter(items) { candidate: PendingInspectionOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingInspectionOverrides(store: PendingInspectionOverrideStore, query: PendingInspectionOverrideQuery): MutableList<PendingInspectionOverride> {
    return query.apply(store.list())
}
