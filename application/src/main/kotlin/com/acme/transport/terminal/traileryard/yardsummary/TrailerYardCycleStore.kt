package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard cycles by code. */
interface TrailerYardCycleStore {
    fun find(code: String): TrailerYardCycle?
    fun save(item: TrailerYardCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardCycleStore(
    var items: MutableMap<String, TrailerYardCycle>,
    var order: MutableList<String>,
) : TrailerYardCycleStore {
    override fun find(code: String): TrailerYardCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardCycle) {
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

    override fun list(): MutableList<TrailerYardCycle> {
        val out = mutableListOf<TrailerYardCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardCycleStore(): TrailerYardCycleStore {
    return MemoryTrailerYardCycleStore(mutableMapOf<String, TrailerYardCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardCycleStore(
    var inner: TrailerYardCycleStore,
    var cache: TtlCache<TrailerYardCycle>,
    var hits: Int,
    var misses: Int,
) : TrailerYardCycleStore {
    override fun find(code: String): TrailerYardCycle? {
        val hit = (cache.get(code) as TrailerYardCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardCycle> = inner.list()
}

fun newCachedTrailerYardCycleStore(inner: TrailerYardCycleStore, ttl: Int, clock: Clock): TrailerYardCycleStore {
    return CachedTrailerYardCycleStore(
        inner = inner,
        cache = newTtlCache<TrailerYardCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard cycles. Empty fields match all. */
data class TrailerYardCycleQuery(
    var title: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardCycle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardCycle>): MutableList<TrailerYardCycle> {
        val matched = filter(items) { candidate: TrailerYardCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardCycles(store: TrailerYardCycleStore, query: TrailerYardCycleQuery): MutableList<TrailerYardCycle> {
    return query.apply(store.list())
}
