package com.acme.transport.terminal.dockdoor.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps adaptive dock door cycles by code. */
interface AdaptiveDockDoorCycleStore {
    fun find(code: String): AdaptiveDockDoorCycle?
    fun save(item: AdaptiveDockDoorCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDockDoorCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDockDoorCycleStore(
    var items: MutableMap<String, AdaptiveDockDoorCycle>,
    var order: MutableList<String>,
) : AdaptiveDockDoorCycleStore {
    override fun find(code: String): AdaptiveDockDoorCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDockDoorCycle) {
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

    override fun list(): MutableList<AdaptiveDockDoorCycle> {
        val out = mutableListOf<AdaptiveDockDoorCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDockDoorCycleStore(): AdaptiveDockDoorCycleStore {
    return MemoryAdaptiveDockDoorCycleStore(
        mutableMapOf<String, AdaptiveDockDoorCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDockDoorCycleStore(
    var inner: AdaptiveDockDoorCycleStore,
    var cache: TtlCache<AdaptiveDockDoorCycle>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDockDoorCycleStore {
    override fun find(code: String): AdaptiveDockDoorCycle? {
        val hit = (cache.get(code) as AdaptiveDockDoorCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDockDoorCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDockDoorCycle> = inner.list()
}

fun newCachedAdaptiveDockDoorCycleStore(inner: AdaptiveDockDoorCycleStore, ttl: Int, clock: Clock): AdaptiveDockDoorCycleStore {
    return CachedAdaptiveDockDoorCycleStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDockDoorCycle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive dock door cycles. Empty fields match all. */
data class AdaptiveDockDoorCycleQuery(
    var label: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDockDoorCycle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveDockDoorCycle>): MutableList<AdaptiveDockDoorCycle> {
        val matched = filter(items) { candidate: AdaptiveDockDoorCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDockDoorCycles(store: AdaptiveDockDoorCycleStore, query: AdaptiveDockDoorCycleQuery): MutableList<AdaptiveDockDoorCycle> {
    return query.apply(store.list())
}
