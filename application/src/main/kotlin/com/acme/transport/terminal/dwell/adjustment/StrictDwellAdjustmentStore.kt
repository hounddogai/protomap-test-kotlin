package com.acme.transport.terminal.dwell.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps strict dwell adjustments by code. */
interface StrictDwellAdjustmentStore {
    fun find(code: String): StrictDwellAdjustment?
    fun save(item: StrictDwellAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDwellAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDwellAdjustmentStore(
    var items: MutableMap<String, StrictDwellAdjustment>,
    var order: MutableList<String>,
) : StrictDwellAdjustmentStore {
    override fun find(code: String): StrictDwellAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDwellAdjustment) {
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

    override fun list(): MutableList<StrictDwellAdjustment> {
        val out = mutableListOf<StrictDwellAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDwellAdjustmentStore(): StrictDwellAdjustmentStore {
    return MemoryStrictDwellAdjustmentStore(
        mutableMapOf<String, StrictDwellAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDwellAdjustmentStore(
    var inner: StrictDwellAdjustmentStore,
    var cache: TtlCache<StrictDwellAdjustment>,
    var hits: Int,
    var misses: Int,
) : StrictDwellAdjustmentStore {
    override fun find(code: String): StrictDwellAdjustment? {
        val hit = (cache.get(code) as StrictDwellAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDwellAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDwellAdjustment> = inner.list()
}

fun newCachedStrictDwellAdjustmentStore(inner: StrictDwellAdjustmentStore, ttl: Int, clock: Clock): StrictDwellAdjustmentStore {
    return CachedStrictDwellAdjustmentStore(
        inner = inner,
        cache = newTtlCache<StrictDwellAdjustment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict dwell adjustments. Empty fields match all. */
data class StrictDwellAdjustmentQuery(
    var country: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDwellAdjustment): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictDwellAdjustment>): MutableList<StrictDwellAdjustment> {
        val matched = filter(items) { candidate: StrictDwellAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDwellAdjustments(store: StrictDwellAdjustmentStore, query: StrictDwellAdjustmentQuery): MutableList<StrictDwellAdjustment> {
    return query.apply(store.list())
}
