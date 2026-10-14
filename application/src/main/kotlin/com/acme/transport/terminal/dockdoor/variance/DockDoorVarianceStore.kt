package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door variances by code. */
interface DockDoorVarianceStore {
    fun find(code: String): DockDoorVariance?
    fun save(item: DockDoorVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorVarianceStore(
    var items: MutableMap<String, DockDoorVariance>,
    var order: MutableList<String>,
) : DockDoorVarianceStore {
    override fun find(code: String): DockDoorVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorVariance) {
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

    override fun list(): MutableList<DockDoorVariance> {
        val out = mutableListOf<DockDoorVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorVarianceStore(): DockDoorVarianceStore {
    return MemoryDockDoorVarianceStore(mutableMapOf<String, DockDoorVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorVarianceStore(
    var inner: DockDoorVarianceStore,
    var cache: TtlCache<DockDoorVariance>,
    var hits: Int,
    var misses: Int,
) : DockDoorVarianceStore {
    override fun find(code: String): DockDoorVariance? {
        val hit = (cache.get(code) as DockDoorVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorVariance> = inner.list()
}

fun newCachedDockDoorVarianceStore(inner: DockDoorVarianceStore, ttl: Int, clock: Clock): DockDoorVarianceStore {
    return CachedDockDoorVarianceStore(
        inner = inner,
        cache = newTtlCache<DockDoorVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door variances. Empty fields match all. */
data class DockDoorVarianceQuery(
    var unit: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorVariance): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<DockDoorVariance>): MutableList<DockDoorVariance> {
        val matched = filter(items) { candidate: DockDoorVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorVariances(store: DockDoorVarianceStore, query: DockDoorVarianceQuery): MutableList<DockDoorVariance> {
    return query.apply(store.list())
}
