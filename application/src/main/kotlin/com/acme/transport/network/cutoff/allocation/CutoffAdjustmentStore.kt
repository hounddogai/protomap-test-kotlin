package com.acme.transport.network.cutoff.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff adjustments by code. */
interface CutoffAdjustmentStore {
    fun find(code: String): CutoffAdjustment?
    fun save(item: CutoffAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffAdjustmentStore(
    var items: MutableMap<String, CutoffAdjustment>,
    var order: MutableList<String>,
) : CutoffAdjustmentStore {
    override fun find(code: String): CutoffAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffAdjustment) {
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

    override fun list(): MutableList<CutoffAdjustment> {
        val out = mutableListOf<CutoffAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffAdjustmentStore(): CutoffAdjustmentStore {
    return MemoryCutoffAdjustmentStore(mutableMapOf<String, CutoffAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffAdjustmentStore(
    var inner: CutoffAdjustmentStore,
    var cache: TtlCache<CutoffAdjustment>,
    var hits: Int,
    var misses: Int,
) : CutoffAdjustmentStore {
    override fun find(code: String): CutoffAdjustment? {
        val hit = (cache.get(code) as CutoffAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffAdjustment> = inner.list()
}

fun newCachedCutoffAdjustmentStore(inner: CutoffAdjustmentStore, ttl: Int, clock: Clock): CutoffAdjustmentStore {
    return CachedCutoffAdjustmentStore(
        inner = inner,
        cache = newTtlCache<CutoffAdjustment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff adjustments. Empty fields match all. */
data class CutoffAdjustmentQuery(
    var label: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffAdjustment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<CutoffAdjustment>): MutableList<CutoffAdjustment> {
        val matched = filter(items) { candidate: CutoffAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffAdjustments(store: CutoffAdjustmentStore, query: CutoffAdjustmentQuery): MutableList<CutoffAdjustment> {
    return query.apply(store.list())
}
