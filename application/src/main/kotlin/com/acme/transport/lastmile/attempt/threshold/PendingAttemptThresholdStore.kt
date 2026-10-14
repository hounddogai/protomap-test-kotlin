package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps pending attempt thresholds by code. */
interface PendingAttemptThresholdStore {
    fun find(code: String): PendingAttemptThreshold?
    fun save(item: PendingAttemptThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingAttemptThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingAttemptThresholdStore(
    var items: MutableMap<String, PendingAttemptThreshold>,
    var order: MutableList<String>,
) : PendingAttemptThresholdStore {
    override fun find(code: String): PendingAttemptThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingAttemptThreshold) {
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

    override fun list(): MutableList<PendingAttemptThreshold> {
        val out = mutableListOf<PendingAttemptThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingAttemptThresholdStore(): PendingAttemptThresholdStore {
    return MemoryPendingAttemptThresholdStore(
        mutableMapOf<String, PendingAttemptThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingAttemptThresholdStore(
    var inner: PendingAttemptThresholdStore,
    var cache: TtlCache<PendingAttemptThreshold>,
    var hits: Int,
    var misses: Int,
) : PendingAttemptThresholdStore {
    override fun find(code: String): PendingAttemptThreshold? {
        val hit = (cache.get(code) as PendingAttemptThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingAttemptThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingAttemptThreshold> = inner.list()
}

fun newCachedPendingAttemptThresholdStore(inner: PendingAttemptThresholdStore, ttl: Int, clock: Clock): PendingAttemptThresholdStore {
    return CachedPendingAttemptThresholdStore(
        inner = inner,
        cache = newTtlCache<PendingAttemptThreshold>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending attempt thresholds. Empty fields match all. */
data class PendingAttemptThresholdQuery(
    var unit: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingAttemptThreshold): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingAttemptThreshold>): MutableList<PendingAttemptThreshold> {
        val matched = filter(items) { candidate: PendingAttemptThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingAttemptThresholds(store: PendingAttemptThresholdStore, query: PendingAttemptThresholdQuery): MutableList<PendingAttemptThreshold> {
    return query.apply(store.list())
}
