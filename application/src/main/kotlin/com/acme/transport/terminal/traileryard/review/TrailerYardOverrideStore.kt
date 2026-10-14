package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard overrides by code. */
interface TrailerYardOverrideStore {
    fun find(code: String): TrailerYardOverride?
    fun save(item: TrailerYardOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardOverrideStore(
    var items: MutableMap<String, TrailerYardOverride>,
    var order: MutableList<String>,
) : TrailerYardOverrideStore {
    override fun find(code: String): TrailerYardOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardOverride) {
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

    override fun list(): MutableList<TrailerYardOverride> {
        val out = mutableListOf<TrailerYardOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardOverrideStore(): TrailerYardOverrideStore {
    return MemoryTrailerYardOverrideStore(mutableMapOf<String, TrailerYardOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardOverrideStore(
    var inner: TrailerYardOverrideStore,
    var cache: TtlCache<TrailerYardOverride>,
    var hits: Int,
    var misses: Int,
) : TrailerYardOverrideStore {
    override fun find(code: String): TrailerYardOverride? {
        val hit = (cache.get(code) as TrailerYardOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardOverride> = inner.list()
}

fun newCachedTrailerYardOverrideStore(inner: TrailerYardOverrideStore, ttl: Int, clock: Clock): TrailerYardOverrideStore {
    return CachedTrailerYardOverrideStore(
        inner = inner,
        cache = newTtlCache<TrailerYardOverride>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard overrides. Empty fields match all. */
data class TrailerYardOverrideQuery(
    var category: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardOverride): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardOverride>): MutableList<TrailerYardOverride> {
        val matched = filter(items) { candidate: TrailerYardOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardOverrides(store: TrailerYardOverrideStore, query: TrailerYardOverrideQuery): MutableList<TrailerYardOverride> {
    return query.apply(store.list())
}
