package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback rest break cycles by code. */
interface FallbackRestBreakCycleStore {
    fun find(code: String): FallbackRestBreakCycle?
    fun save(item: FallbackRestBreakCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRestBreakCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRestBreakCycleStore(
    var items: MutableMap<String, FallbackRestBreakCycle>,
    var order: MutableList<String>,
) : FallbackRestBreakCycleStore {
    override fun find(code: String): FallbackRestBreakCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRestBreakCycle) {
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

    override fun list(): MutableList<FallbackRestBreakCycle> {
        val out = mutableListOf<FallbackRestBreakCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRestBreakCycleStore(): FallbackRestBreakCycleStore {
    return MemoryFallbackRestBreakCycleStore(
        mutableMapOf<String, FallbackRestBreakCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRestBreakCycleStore(
    var inner: FallbackRestBreakCycleStore,
    var cache: TtlCache<FallbackRestBreakCycle>,
    var hits: Int,
    var misses: Int,
) : FallbackRestBreakCycleStore {
    override fun find(code: String): FallbackRestBreakCycle? {
        val hit = (cache.get(code) as FallbackRestBreakCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRestBreakCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRestBreakCycle> = inner.list()
}

fun newCachedFallbackRestBreakCycleStore(inner: FallbackRestBreakCycleStore, ttl: Int, clock: Clock): FallbackRestBreakCycleStore {
    return CachedFallbackRestBreakCycleStore(
        inner = inner,
        cache = newTtlCache<FallbackRestBreakCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback rest break cycles. Empty fields match all. */
data class FallbackRestBreakCycleQuery(
    var name: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRestBreakCycle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<FallbackRestBreakCycle>): MutableList<FallbackRestBreakCycle> {
        val matched = filter(items) { candidate: FallbackRestBreakCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRestBreakCycles(store: FallbackRestBreakCycleStore, query: FallbackRestBreakCycleQuery): MutableList<FallbackRestBreakCycle> {
    return query.apply(store.list())
}
