package com.acme.transport.network.node.nodecycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps fallback node cycles by code. */
interface FallbackNodeCycleStore {
    fun find(code: String): FallbackNodeCycle?
    fun save(item: FallbackNodeCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackNodeCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackNodeCycleStore(
    var items: MutableMap<String, FallbackNodeCycle>,
    var order: MutableList<String>,
) : FallbackNodeCycleStore {
    override fun find(code: String): FallbackNodeCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackNodeCycle) {
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

    override fun list(): MutableList<FallbackNodeCycle> {
        val out = mutableListOf<FallbackNodeCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackNodeCycleStore(): FallbackNodeCycleStore {
    return MemoryFallbackNodeCycleStore(mutableMapOf<String, FallbackNodeCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackNodeCycleStore(
    var inner: FallbackNodeCycleStore,
    var cache: TtlCache<FallbackNodeCycle>,
    var hits: Int,
    var misses: Int,
) : FallbackNodeCycleStore {
    override fun find(code: String): FallbackNodeCycle? {
        val hit = (cache.get(code) as FallbackNodeCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackNodeCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackNodeCycle> = inner.list()
}

fun newCachedFallbackNodeCycleStore(inner: FallbackNodeCycleStore, ttl: Int, clock: Clock): FallbackNodeCycleStore {
    return CachedFallbackNodeCycleStore(
        inner = inner,
        cache = newTtlCache<FallbackNodeCycle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback node cycles. Empty fields match all. */
data class FallbackNodeCycleQuery(
    var name: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackNodeCycle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<FallbackNodeCycle>): MutableList<FallbackNodeCycle> {
        val matched = filter(items) { candidate: FallbackNodeCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackNodeCycles(store: FallbackNodeCycleStore, query: FallbackNodeCycleQuery): MutableList<FallbackNodeCycle> {
    return query.apply(store.list())
}
