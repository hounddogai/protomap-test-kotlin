package com.acme.transport.crewing.restbreak.entry

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break cycles by code. */
interface RestBreakCycleStore {
    fun find(code: String): RestBreakCycle?
    fun save(item: RestBreakCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakCycleStore(
    var items: MutableMap<String, RestBreakCycle>,
    var order: MutableList<String>,
) : RestBreakCycleStore {
    override fun find(code: String): RestBreakCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakCycle) {
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

    override fun list(): MutableList<RestBreakCycle> {
        val out = mutableListOf<RestBreakCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakCycleStore(): RestBreakCycleStore {
    return MemoryRestBreakCycleStore(mutableMapOf<String, RestBreakCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakCycleStore(
    var inner: RestBreakCycleStore,
    var cache: TtlCache<RestBreakCycle>,
    var hits: Int,
    var misses: Int,
) : RestBreakCycleStore {
    override fun find(code: String): RestBreakCycle? {
        val hit = (cache.get(code) as RestBreakCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakCycle> = inner.list()
}

fun newCachedRestBreakCycleStore(inner: RestBreakCycleStore, ttl: Int, clock: Clock): RestBreakCycleStore {
    return CachedRestBreakCycleStore(
        inner = inner,
        cache = newTtlCache<RestBreakCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break cycles. Empty fields match all. */
data class RestBreakCycleQuery(
    var lane: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakCycle): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakCycle>): MutableList<RestBreakCycle> {
        val matched = filter(items) { candidate: RestBreakCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakCycles(store: RestBreakCycleStore, query: RestBreakCycleQuery): MutableList<RestBreakCycle> {
    return query.apply(store.list())
}
