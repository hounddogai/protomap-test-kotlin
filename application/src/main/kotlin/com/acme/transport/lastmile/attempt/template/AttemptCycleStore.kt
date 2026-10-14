package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt cycles by code. */
interface AttemptCycleStore {
    fun find(code: String): AttemptCycle?
    fun save(item: AttemptCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptCycleStore(
    var items: MutableMap<String, AttemptCycle>,
    var order: MutableList<String>,
) : AttemptCycleStore {
    override fun find(code: String): AttemptCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptCycle) {
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

    override fun list(): MutableList<AttemptCycle> {
        val out = mutableListOf<AttemptCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptCycleStore(): AttemptCycleStore {
    return MemoryAttemptCycleStore(mutableMapOf<String, AttemptCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptCycleStore(
    var inner: AttemptCycleStore,
    var cache: TtlCache<AttemptCycle>,
    var hits: Int,
    var misses: Int,
) : AttemptCycleStore {
    override fun find(code: String): AttemptCycle? {
        val hit = (cache.get(code) as AttemptCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptCycle> = inner.list()
}

fun newCachedAttemptCycleStore(inner: AttemptCycleStore, ttl: Int, clock: Clock): AttemptCycleStore {
    return CachedAttemptCycleStore(
        inner = inner,
        cache = newTtlCache<AttemptCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt cycles. Empty fields match all. */
data class AttemptCycleQuery(
    var depot: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptCycle): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptCycle>): MutableList<AttemptCycle> {
        val matched = filter(items) { candidate: AttemptCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptCycles(store: AttemptCycleStore, query: AttemptCycleQuery): MutableList<AttemptCycle> {
    return query.apply(store.list())
}
