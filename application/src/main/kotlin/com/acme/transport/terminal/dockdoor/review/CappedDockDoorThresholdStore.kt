package com.acme.transport.terminal.dockdoor.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps capped dock door thresholds by code. */
interface CappedDockDoorThresholdStore {
    fun find(code: String): CappedDockDoorThreshold?
    fun save(item: CappedDockDoorThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedDockDoorThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedDockDoorThresholdStore(
    var items: MutableMap<String, CappedDockDoorThreshold>,
    var order: MutableList<String>,
) : CappedDockDoorThresholdStore {
    override fun find(code: String): CappedDockDoorThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedDockDoorThreshold) {
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

    override fun list(): MutableList<CappedDockDoorThreshold> {
        val out = mutableListOf<CappedDockDoorThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedDockDoorThresholdStore(): CappedDockDoorThresholdStore {
    return MemoryCappedDockDoorThresholdStore(
        mutableMapOf<String, CappedDockDoorThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedDockDoorThresholdStore(
    var inner: CappedDockDoorThresholdStore,
    var cache: TtlCache<CappedDockDoorThreshold>,
    var hits: Int,
    var misses: Int,
) : CappedDockDoorThresholdStore {
    override fun find(code: String): CappedDockDoorThreshold? {
        val hit = (cache.get(code) as CappedDockDoorThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedDockDoorThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedDockDoorThreshold> = inner.list()
}

fun newCachedCappedDockDoorThresholdStore(inner: CappedDockDoorThresholdStore, ttl: Int, clock: Clock): CappedDockDoorThresholdStore {
    return CachedCappedDockDoorThresholdStore(
        inner = inner,
        cache = newTtlCache<CappedDockDoorThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped dock door thresholds. Empty fields match all. */
data class CappedDockDoorThresholdQuery(
    var depot: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedDockDoorThreshold): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedDockDoorThreshold>): MutableList<CappedDockDoorThreshold> {
        val matched = filter(items) { candidate: CappedDockDoorThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedDockDoorThresholds(store: CappedDockDoorThresholdStore, query: CappedDockDoorThresholdQuery): MutableList<CappedDockDoorThreshold> {
    return query.apply(store.list())
}
