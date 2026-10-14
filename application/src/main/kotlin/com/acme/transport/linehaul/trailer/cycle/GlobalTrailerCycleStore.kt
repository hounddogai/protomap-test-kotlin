package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps global trailer cycles by code. */
interface GlobalTrailerCycleStore {
    fun find(code: String): GlobalTrailerCycle?
    fun save(item: GlobalTrailerCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalTrailerCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalTrailerCycleStore(
    var items: MutableMap<String, GlobalTrailerCycle>,
    var order: MutableList<String>,
) : GlobalTrailerCycleStore {
    override fun find(code: String): GlobalTrailerCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalTrailerCycle) {
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

    override fun list(): MutableList<GlobalTrailerCycle> {
        val out = mutableListOf<GlobalTrailerCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalTrailerCycleStore(): GlobalTrailerCycleStore {
    return MemoryGlobalTrailerCycleStore(mutableMapOf<String, GlobalTrailerCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalTrailerCycleStore(
    var inner: GlobalTrailerCycleStore,
    var cache: TtlCache<GlobalTrailerCycle>,
    var hits: Int,
    var misses: Int,
) : GlobalTrailerCycleStore {
    override fun find(code: String): GlobalTrailerCycle? {
        val hit = (cache.get(code) as GlobalTrailerCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalTrailerCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalTrailerCycle> = inner.list()
}

fun newCachedGlobalTrailerCycleStore(inner: GlobalTrailerCycleStore, ttl: Int, clock: Clock): GlobalTrailerCycleStore {
    return CachedGlobalTrailerCycleStore(
        inner = inner,
        cache = newTtlCache<GlobalTrailerCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global trailer cycles. Empty fields match all. */
data class GlobalTrailerCycleQuery(
    var country: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalTrailerCycle): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalTrailerCycle>): MutableList<GlobalTrailerCycle> {
        val matched = filter(items) { candidate: GlobalTrailerCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalTrailerCycles(store: GlobalTrailerCycleStore, query: GlobalTrailerCycleQuery): MutableList<GlobalTrailerCycle> {
    return query.apply(store.list())
}
