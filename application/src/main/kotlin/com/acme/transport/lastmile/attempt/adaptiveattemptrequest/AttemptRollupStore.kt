package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt rollups by code. */
interface AttemptRollupStore {
    fun find(code: String): AttemptRollup?
    fun save(item: AttemptRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptRollupStore(
    var items: MutableMap<String, AttemptRollup>,
    var order: MutableList<String>,
) : AttemptRollupStore {
    override fun find(code: String): AttemptRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptRollup) {
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

    override fun list(): MutableList<AttemptRollup> {
        val out = mutableListOf<AttemptRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptRollupStore(): AttemptRollupStore {
    return MemoryAttemptRollupStore(mutableMapOf<String, AttemptRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptRollupStore(
    var inner: AttemptRollupStore,
    var cache: TtlCache<AttemptRollup>,
    var hits: Int,
    var misses: Int,
) : AttemptRollupStore {
    override fun find(code: String): AttemptRollup? {
        val hit = (cache.get(code) as AttemptRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptRollup> = inner.list()
}

fun newCachedAttemptRollupStore(inner: AttemptRollupStore, ttl: Int, clock: Clock): AttemptRollupStore {
    return CachedAttemptRollupStore(
        inner = inner,
        cache = newTtlCache<AttemptRollup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt rollups. Empty fields match all. */
data class AttemptRollupQuery(
    var category: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptRollup): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptRollup>): MutableList<AttemptRollup> {
        val matched = filter(items) { candidate: AttemptRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptRollups(store: AttemptRollupStore, query: AttemptRollupQuery): MutableList<AttemptRollup> {
    return query.apply(store.list())
}
