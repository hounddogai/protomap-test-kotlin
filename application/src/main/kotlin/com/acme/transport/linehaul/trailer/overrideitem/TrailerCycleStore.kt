package com.acme.transport.linehaul.trailer.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer cycles by code. */
interface TrailerCycleStore {
    fun find(code: String): TrailerCycle?
    fun save(item: TrailerCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerCycleStore(
    var items: MutableMap<String, TrailerCycle>,
    var order: MutableList<String>,
) : TrailerCycleStore {
    override fun find(code: String): TrailerCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerCycle) {
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

    override fun list(): MutableList<TrailerCycle> {
        val out = mutableListOf<TrailerCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerCycleStore(): TrailerCycleStore {
    return MemoryTrailerCycleStore(mutableMapOf<String, TrailerCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerCycleStore(
    var inner: TrailerCycleStore,
    var cache: TtlCache<TrailerCycle>,
    var hits: Int,
    var misses: Int,
) : TrailerCycleStore {
    override fun find(code: String): TrailerCycle? {
        val hit = (cache.get(code) as TrailerCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerCycle> = inner.list()
}

fun newCachedTrailerCycleStore(inner: TrailerCycleStore, ttl: Int, clock: Clock): TrailerCycleStore {
    return CachedTrailerCycleStore(
        inner = inner,
        cache = newTtlCache<TrailerCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer cycles. Empty fields match all. */
data class TrailerCycleQuery(
    var country: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerCycle): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerCycle>): MutableList<TrailerCycle> {
        val matched = filter(items) { candidate: TrailerCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerCycles(store: TrailerCycleStore, query: TrailerCycleQuery): MutableList<TrailerCycle> {
    return query.apply(store.list())
}
