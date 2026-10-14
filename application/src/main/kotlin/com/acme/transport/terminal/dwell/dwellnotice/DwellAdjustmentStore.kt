package com.acme.transport.terminal.dwell.dwellnotice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell adjustments by code. */
interface DwellAdjustmentStore {
    fun find(code: String): DwellAdjustment?
    fun save(item: DwellAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellAdjustmentStore(
    var items: MutableMap<String, DwellAdjustment>,
    var order: MutableList<String>,
) : DwellAdjustmentStore {
    override fun find(code: String): DwellAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellAdjustment) {
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

    override fun list(): MutableList<DwellAdjustment> {
        val out = mutableListOf<DwellAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellAdjustmentStore(): DwellAdjustmentStore {
    return MemoryDwellAdjustmentStore(mutableMapOf<String, DwellAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellAdjustmentStore(
    var inner: DwellAdjustmentStore,
    var cache: TtlCache<DwellAdjustment>,
    var hits: Int,
    var misses: Int,
) : DwellAdjustmentStore {
    override fun find(code: String): DwellAdjustment? {
        val hit = (cache.get(code) as DwellAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellAdjustment> = inner.list()
}

fun newCachedDwellAdjustmentStore(inner: DwellAdjustmentStore, ttl: Int, clock: Clock): DwellAdjustmentStore {
    return CachedDwellAdjustmentStore(
        inner = inner,
        cache = newTtlCache<DwellAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell adjustments. Empty fields match all. */
data class DwellAdjustmentQuery(
    var name: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellAdjustment>): MutableList<DwellAdjustment> {
        val matched = filter(items) { candidate: DwellAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellAdjustments(store: DwellAdjustmentStore, query: DwellAdjustmentQuery): MutableList<DwellAdjustment> {
    return query.apply(store.list())
}
