package com.acme.transport.terminal.traileryard.yardcycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps primary trailer yard cycles by code. */
interface PrimaryTrailerYardCycleStore {
    fun find(code: String): PrimaryTrailerYardCycle?
    fun save(item: PrimaryTrailerYardCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTrailerYardCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTrailerYardCycleStore(
    var items: MutableMap<String, PrimaryTrailerYardCycle>,
    var order: MutableList<String>,
) : PrimaryTrailerYardCycleStore {
    override fun find(code: String): PrimaryTrailerYardCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTrailerYardCycle) {
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

    override fun list(): MutableList<PrimaryTrailerYardCycle> {
        val out = mutableListOf<PrimaryTrailerYardCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTrailerYardCycleStore(): PrimaryTrailerYardCycleStore {
    return MemoryPrimaryTrailerYardCycleStore(
        mutableMapOf<String, PrimaryTrailerYardCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTrailerYardCycleStore(
    var inner: PrimaryTrailerYardCycleStore,
    var cache: TtlCache<PrimaryTrailerYardCycle>,
    var hits: Int,
    var misses: Int,
) : PrimaryTrailerYardCycleStore {
    override fun find(code: String): PrimaryTrailerYardCycle? {
        val hit = (cache.get(code) as PrimaryTrailerYardCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTrailerYardCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTrailerYardCycle> = inner.list()
}

fun newCachedPrimaryTrailerYardCycleStore(inner: PrimaryTrailerYardCycleStore, ttl: Int, clock: Clock): PrimaryTrailerYardCycleStore {
    return CachedPrimaryTrailerYardCycleStore(
        inner = inner,
        cache = newTtlCache<PrimaryTrailerYardCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary trailer yard cycles. Empty fields match all. */
data class PrimaryTrailerYardCycleQuery(
    var segment: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTrailerYardCycle): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTrailerYardCycle>): MutableList<PrimaryTrailerYardCycle> {
        val matched = filter(items) { candidate: PrimaryTrailerYardCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTrailerYardCycles(store: PrimaryTrailerYardCycleStore, query: PrimaryTrailerYardCycleQuery): MutableList<PrimaryTrailerYardCycle> {
    return query.apply(store.list())
}
