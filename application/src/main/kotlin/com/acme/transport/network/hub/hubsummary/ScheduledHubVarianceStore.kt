package com.acme.transport.network.hub.hubsummary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps scheduled hub variances by code. */
interface ScheduledHubVarianceStore {
    fun find(code: String): ScheduledHubVariance?
    fun save(item: ScheduledHubVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledHubVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledHubVarianceStore(
    var items: MutableMap<String, ScheduledHubVariance>,
    var order: MutableList<String>,
) : ScheduledHubVarianceStore {
    override fun find(code: String): ScheduledHubVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledHubVariance) {
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

    override fun list(): MutableList<ScheduledHubVariance> {
        val out = mutableListOf<ScheduledHubVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledHubVarianceStore(): ScheduledHubVarianceStore {
    return MemoryScheduledHubVarianceStore(
        mutableMapOf<String, ScheduledHubVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledHubVarianceStore(
    var inner: ScheduledHubVarianceStore,
    var cache: TtlCache<ScheduledHubVariance>,
    var hits: Int,
    var misses: Int,
) : ScheduledHubVarianceStore {
    override fun find(code: String): ScheduledHubVariance? {
        val hit = (cache.get(code) as ScheduledHubVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledHubVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledHubVariance> = inner.list()
}

fun newCachedScheduledHubVarianceStore(inner: ScheduledHubVarianceStore, ttl: Int, clock: Clock): ScheduledHubVarianceStore {
    return CachedScheduledHubVarianceStore(
        inner = inner,
        cache = newTtlCache<ScheduledHubVariance>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled hub variances. Empty fields match all. */
data class ScheduledHubVarianceQuery(
    var unit: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledHubVariance): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledHubVariance>): MutableList<ScheduledHubVariance> {
        val matched = filter(items) { candidate: ScheduledHubVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledHubVariances(store: ScheduledHubVarianceStore, query: ScheduledHubVarianceQuery): MutableList<ScheduledHubVariance> {
    return query.apply(store.list())
}
