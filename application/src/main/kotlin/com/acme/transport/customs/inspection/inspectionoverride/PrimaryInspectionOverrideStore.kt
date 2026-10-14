package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary inspection overrides by code. */
interface PrimaryInspectionOverrideStore {
    fun find(code: String): PrimaryInspectionOverride?
    fun save(item: PrimaryInspectionOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryInspectionOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryInspectionOverrideStore(
    var items: MutableMap<String, PrimaryInspectionOverride>,
    var order: MutableList<String>,
) : PrimaryInspectionOverrideStore {
    override fun find(code: String): PrimaryInspectionOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryInspectionOverride) {
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

    override fun list(): MutableList<PrimaryInspectionOverride> {
        val out = mutableListOf<PrimaryInspectionOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryInspectionOverrideStore(): PrimaryInspectionOverrideStore {
    return MemoryPrimaryInspectionOverrideStore(
        mutableMapOf<String, PrimaryInspectionOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryInspectionOverrideStore(
    var inner: PrimaryInspectionOverrideStore,
    var cache: TtlCache<PrimaryInspectionOverride>,
    var hits: Int,
    var misses: Int,
) : PrimaryInspectionOverrideStore {
    override fun find(code: String): PrimaryInspectionOverride? {
        val hit = (cache.get(code) as PrimaryInspectionOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryInspectionOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryInspectionOverride> = inner.list()
}

fun newCachedPrimaryInspectionOverrideStore(inner: PrimaryInspectionOverrideStore, ttl: Int, clock: Clock): PrimaryInspectionOverrideStore {
    return CachedPrimaryInspectionOverrideStore(
        inner = inner,
        cache = newTtlCache<PrimaryInspectionOverride>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary inspection overrides. Empty fields match all. */
data class PrimaryInspectionOverrideQuery(
    var channel: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryInspectionOverride): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<PrimaryInspectionOverride>): MutableList<PrimaryInspectionOverride> {
        val matched = filter(items) { candidate: PrimaryInspectionOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryInspectionOverrides(store: PrimaryInspectionOverrideStore, query: PrimaryInspectionOverrideQuery): MutableList<PrimaryInspectionOverride> {
    return query.apply(store.list())
}
