package com.acme.transport.lastmile.attempt.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt adjustments by code. */
interface AttemptAdjustmentStore {
    fun find(code: String): AttemptAdjustment?
    fun save(item: AttemptAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptAdjustmentStore(
    var items: MutableMap<String, AttemptAdjustment>,
    var order: MutableList<String>,
) : AttemptAdjustmentStore {
    override fun find(code: String): AttemptAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptAdjustment) {
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

    override fun list(): MutableList<AttemptAdjustment> {
        val out = mutableListOf<AttemptAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptAdjustmentStore(): AttemptAdjustmentStore {
    return MemoryAttemptAdjustmentStore(mutableMapOf<String, AttemptAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptAdjustmentStore(
    var inner: AttemptAdjustmentStore,
    var cache: TtlCache<AttemptAdjustment>,
    var hits: Int,
    var misses: Int,
) : AttemptAdjustmentStore {
    override fun find(code: String): AttemptAdjustment? {
        val hit = (cache.get(code) as AttemptAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptAdjustment> = inner.list()
}

fun newCachedAttemptAdjustmentStore(inner: AttemptAdjustmentStore, ttl: Int, clock: Clock): AttemptAdjustmentStore {
    return CachedAttemptAdjustmentStore(
        inner = inner,
        cache = newTtlCache<AttemptAdjustment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt adjustments. Empty fields match all. */
data class AttemptAdjustmentQuery(
    var channel: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptAdjustment): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptAdjustment>): MutableList<AttemptAdjustment> {
        val matched = filter(items) { candidate: AttemptAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptAdjustments(store: AttemptAdjustmentStore, query: AttemptAdjustmentQuery): MutableList<AttemptAdjustment> {
    return query.apply(store.list())
}
