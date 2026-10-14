package com.acme.transport.customs.inspection.allocation

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending inspection rollups by code. */
interface PendingInspectionRollupStore {
    fun find(code: String): PendingInspectionRollup?
    fun save(item: PendingInspectionRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingInspectionRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingInspectionRollupStore(
    var items: MutableMap<String, PendingInspectionRollup>,
    var order: MutableList<String>,
) : PendingInspectionRollupStore {
    override fun find(code: String): PendingInspectionRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingInspectionRollup) {
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

    override fun list(): MutableList<PendingInspectionRollup> {
        val out = mutableListOf<PendingInspectionRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingInspectionRollupStore(): PendingInspectionRollupStore {
    return MemoryPendingInspectionRollupStore(
        mutableMapOf<String, PendingInspectionRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingInspectionRollupStore(
    var inner: PendingInspectionRollupStore,
    var cache: TtlCache<PendingInspectionRollup>,
    var hits: Int,
    var misses: Int,
) : PendingInspectionRollupStore {
    override fun find(code: String): PendingInspectionRollup? {
        val hit = (cache.get(code) as PendingInspectionRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingInspectionRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingInspectionRollup> = inner.list()
}

fun newCachedPendingInspectionRollupStore(inner: PendingInspectionRollupStore, ttl: Int, clock: Clock): PendingInspectionRollupStore {
    return CachedPendingInspectionRollupStore(
        inner = inner,
        cache = newTtlCache<PendingInspectionRollup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending inspection rollups. Empty fields match all. */
data class PendingInspectionRollupQuery(
    var label: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingInspectionRollup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingInspectionRollup>): MutableList<PendingInspectionRollup> {
        val matched = filter(items) { candidate: PendingInspectionRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingInspectionRollups(store: PendingInspectionRollupStore, query: PendingInspectionRollupQuery): MutableList<PendingInspectionRollup> {
    return query.apply(store.list())
}
