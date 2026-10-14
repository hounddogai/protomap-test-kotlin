package com.acme.transport.fleetops.defect.budget

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled defect thresholds by code. */
interface ScheduledDefectThresholdStore {
    fun find(code: String): ScheduledDefectThreshold?
    fun save(item: ScheduledDefectThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDefectThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDefectThresholdStore(
    var items: MutableMap<String, ScheduledDefectThreshold>,
    var order: MutableList<String>,
) : ScheduledDefectThresholdStore {
    override fun find(code: String): ScheduledDefectThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDefectThreshold) {
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

    override fun list(): MutableList<ScheduledDefectThreshold> {
        val out = mutableListOf<ScheduledDefectThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDefectThresholdStore(): ScheduledDefectThresholdStore {
    return MemoryScheduledDefectThresholdStore(
        mutableMapOf<String, ScheduledDefectThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDefectThresholdStore(
    var inner: ScheduledDefectThresholdStore,
    var cache: TtlCache<ScheduledDefectThreshold>,
    var hits: Int,
    var misses: Int,
) : ScheduledDefectThresholdStore {
    override fun find(code: String): ScheduledDefectThreshold? {
        val hit = (cache.get(code) as ScheduledDefectThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDefectThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDefectThreshold> = inner.list()
}

fun newCachedScheduledDefectThresholdStore(inner: ScheduledDefectThresholdStore, ttl: Int, clock: Clock): ScheduledDefectThresholdStore {
    return CachedScheduledDefectThresholdStore(
        inner = inner,
        cache = newTtlCache<ScheduledDefectThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled defect thresholds. Empty fields match all. */
data class ScheduledDefectThresholdQuery(
    var channel: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDefectThreshold): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledDefectThreshold>): MutableList<ScheduledDefectThreshold> {
        val matched = filter(items) { candidate: ScheduledDefectThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDefectThresholds(store: ScheduledDefectThresholdStore, query: ScheduledDefectThresholdQuery): MutableList<ScheduledDefectThreshold> {
    return query.apply(store.list())
}
