package com.acme.transport.terminal.dockdoor.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door thresholds by code. */
interface DockDoorThresholdStore {
    fun find(code: String): DockDoorThreshold?
    fun save(item: DockDoorThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorThresholdStore(
    var items: MutableMap<String, DockDoorThreshold>,
    var order: MutableList<String>,
) : DockDoorThresholdStore {
    override fun find(code: String): DockDoorThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorThreshold) {
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

    override fun list(): MutableList<DockDoorThreshold> {
        val out = mutableListOf<DockDoorThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorThresholdStore(): DockDoorThresholdStore {
    return MemoryDockDoorThresholdStore(mutableMapOf<String, DockDoorThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorThresholdStore(
    var inner: DockDoorThresholdStore,
    var cache: TtlCache<DockDoorThreshold>,
    var hits: Int,
    var misses: Int,
) : DockDoorThresholdStore {
    override fun find(code: String): DockDoorThreshold? {
        val hit = (cache.get(code) as DockDoorThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorThreshold> = inner.list()
}

fun newCachedDockDoorThresholdStore(inner: DockDoorThresholdStore, ttl: Int, clock: Clock): DockDoorThresholdStore {
    return CachedDockDoorThresholdStore(
        inner = inner,
        cache = newTtlCache<DockDoorThreshold>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door thresholds. Empty fields match all. */
data class DockDoorThresholdQuery(
    var channel: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorThreshold): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorThreshold>): MutableList<DockDoorThreshold> {
        val matched = filter(items) { candidate: DockDoorThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorThresholds(store: DockDoorThresholdStore, query: DockDoorThresholdQuery): MutableList<DockDoorThreshold> {
    return query.apply(store.list())
}
