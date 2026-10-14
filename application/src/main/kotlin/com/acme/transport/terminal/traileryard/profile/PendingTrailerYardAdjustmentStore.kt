package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps pending trailer yard adjustments by code. */
interface PendingTrailerYardAdjustmentStore {
    fun find(code: String): PendingTrailerYardAdjustment?
    fun save(item: PendingTrailerYardAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingTrailerYardAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingTrailerYardAdjustmentStore(
    var items: MutableMap<String, PendingTrailerYardAdjustment>,
    var order: MutableList<String>,
) : PendingTrailerYardAdjustmentStore {
    override fun find(code: String): PendingTrailerYardAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingTrailerYardAdjustment) {
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

    override fun list(): MutableList<PendingTrailerYardAdjustment> {
        val out = mutableListOf<PendingTrailerYardAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingTrailerYardAdjustmentStore(): PendingTrailerYardAdjustmentStore {
    return MemoryPendingTrailerYardAdjustmentStore(
        mutableMapOf<String, PendingTrailerYardAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingTrailerYardAdjustmentStore(
    var inner: PendingTrailerYardAdjustmentStore,
    var cache: TtlCache<PendingTrailerYardAdjustment>,
    var hits: Int,
    var misses: Int,
) : PendingTrailerYardAdjustmentStore {
    override fun find(code: String): PendingTrailerYardAdjustment? {
        val hit = (cache.get(code) as PendingTrailerYardAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingTrailerYardAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingTrailerYardAdjustment> = inner.list()
}

fun newCachedPendingTrailerYardAdjustmentStore(inner: PendingTrailerYardAdjustmentStore, ttl: Int, clock: Clock): PendingTrailerYardAdjustmentStore {
    return CachedPendingTrailerYardAdjustmentStore(
        inner = inner,
        cache = newTtlCache<PendingTrailerYardAdjustment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending trailer yard adjustments. Empty fields match all. */
data class PendingTrailerYardAdjustmentQuery(
    var label: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingTrailerYardAdjustment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingTrailerYardAdjustment>): MutableList<PendingTrailerYardAdjustment> {
        val matched = filter(items) { candidate: PendingTrailerYardAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingTrailerYardAdjustments(store: PendingTrailerYardAdjustmentStore, query: PendingTrailerYardAdjustmentQuery): MutableList<PendingTrailerYardAdjustment> {
    return query.apply(store.list())
}
