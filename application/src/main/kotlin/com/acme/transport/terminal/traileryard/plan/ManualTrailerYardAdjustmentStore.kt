package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps manual trailer yard adjustments by code. */
interface ManualTrailerYardAdjustmentStore {
    fun find(code: String): ManualTrailerYardAdjustment?
    fun save(item: ManualTrailerYardAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualTrailerYardAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualTrailerYardAdjustmentStore(
    var items: MutableMap<String, ManualTrailerYardAdjustment>,
    var order: MutableList<String>,
) : ManualTrailerYardAdjustmentStore {
    override fun find(code: String): ManualTrailerYardAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualTrailerYardAdjustment) {
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

    override fun list(): MutableList<ManualTrailerYardAdjustment> {
        val out = mutableListOf<ManualTrailerYardAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualTrailerYardAdjustmentStore(): ManualTrailerYardAdjustmentStore {
    return MemoryManualTrailerYardAdjustmentStore(
        mutableMapOf<String, ManualTrailerYardAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualTrailerYardAdjustmentStore(
    var inner: ManualTrailerYardAdjustmentStore,
    var cache: TtlCache<ManualTrailerYardAdjustment>,
    var hits: Int,
    var misses: Int,
) : ManualTrailerYardAdjustmentStore {
    override fun find(code: String): ManualTrailerYardAdjustment? {
        val hit = (cache.get(code) as ManualTrailerYardAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualTrailerYardAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualTrailerYardAdjustment> = inner.list()
}

fun newCachedManualTrailerYardAdjustmentStore(inner: ManualTrailerYardAdjustmentStore, ttl: Int, clock: Clock): ManualTrailerYardAdjustmentStore {
    return CachedManualTrailerYardAdjustmentStore(
        inner = inner,
        cache = newTtlCache<ManualTrailerYardAdjustment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual trailer yard adjustments. Empty fields match all. */
data class ManualTrailerYardAdjustmentQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualTrailerYardAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualTrailerYardAdjustment>): MutableList<ManualTrailerYardAdjustment> {
        val matched = filter(items) { candidate: ManualTrailerYardAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualTrailerYardAdjustments(store: ManualTrailerYardAdjustmentStore, query: ManualTrailerYardAdjustmentQuery): MutableList<ManualTrailerYardAdjustment> {
    return query.apply(store.list())
}
