package com.acme.transport.lastmile.attempt.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps strict attempt thresholds by code. */
interface StrictAttemptThresholdStore {
    fun find(code: String): StrictAttemptThreshold?
    fun save(item: StrictAttemptThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictAttemptThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictAttemptThresholdStore(
    var items: MutableMap<String, StrictAttemptThreshold>,
    var order: MutableList<String>,
) : StrictAttemptThresholdStore {
    override fun find(code: String): StrictAttemptThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictAttemptThreshold) {
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

    override fun list(): MutableList<StrictAttemptThreshold> {
        val out = mutableListOf<StrictAttemptThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictAttemptThresholdStore(): StrictAttemptThresholdStore {
    return MemoryStrictAttemptThresholdStore(
        mutableMapOf<String, StrictAttemptThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictAttemptThresholdStore(
    var inner: StrictAttemptThresholdStore,
    var cache: TtlCache<StrictAttemptThreshold>,
    var hits: Int,
    var misses: Int,
) : StrictAttemptThresholdStore {
    override fun find(code: String): StrictAttemptThreshold? {
        val hit = (cache.get(code) as StrictAttemptThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictAttemptThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictAttemptThreshold> = inner.list()
}

fun newCachedStrictAttemptThresholdStore(inner: StrictAttemptThresholdStore, ttl: Int, clock: Clock): StrictAttemptThresholdStore {
    return CachedStrictAttemptThresholdStore(
        inner = inner,
        cache = newTtlCache<StrictAttemptThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict attempt thresholds. Empty fields match all. */
data class StrictAttemptThresholdQuery(
    var label: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictAttemptThreshold): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictAttemptThreshold>): MutableList<StrictAttemptThreshold> {
        val matched = filter(items) { candidate: StrictAttemptThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictAttemptThresholds(store: StrictAttemptThresholdStore, query: StrictAttemptThresholdQuery): MutableList<StrictAttemptThreshold> {
    return query.apply(store.list())
}
