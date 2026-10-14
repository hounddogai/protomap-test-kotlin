package com.acme.transport.customs.broker.threshold

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled broker thresholds by code. */
interface ScheduledBrokerThresholdStore {
    fun find(code: String): ScheduledBrokerThreshold?
    fun save(item: ScheduledBrokerThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledBrokerThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledBrokerThresholdStore(
    var items: MutableMap<String, ScheduledBrokerThreshold>,
    var order: MutableList<String>,
) : ScheduledBrokerThresholdStore {
    override fun find(code: String): ScheduledBrokerThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledBrokerThreshold) {
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

    override fun list(): MutableList<ScheduledBrokerThreshold> {
        val out = mutableListOf<ScheduledBrokerThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledBrokerThresholdStore(): ScheduledBrokerThresholdStore {
    return MemoryScheduledBrokerThresholdStore(
        mutableMapOf<String, ScheduledBrokerThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledBrokerThresholdStore(
    var inner: ScheduledBrokerThresholdStore,
    var cache: TtlCache<ScheduledBrokerThreshold>,
    var hits: Int,
    var misses: Int,
) : ScheduledBrokerThresholdStore {
    override fun find(code: String): ScheduledBrokerThreshold? {
        val hit = (cache.get(code) as ScheduledBrokerThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledBrokerThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledBrokerThreshold> = inner.list()
}

fun newCachedScheduledBrokerThresholdStore(inner: ScheduledBrokerThresholdStore, ttl: Int, clock: Clock): ScheduledBrokerThresholdStore {
    return CachedScheduledBrokerThresholdStore(
        inner = inner,
        cache = newTtlCache<ScheduledBrokerThreshold>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled broker thresholds. Empty fields match all. */
data class ScheduledBrokerThresholdQuery(
    var title: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledBrokerThreshold): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<ScheduledBrokerThreshold>): MutableList<ScheduledBrokerThreshold> {
        val matched = filter(items) { candidate: ScheduledBrokerThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledBrokerThresholds(store: ScheduledBrokerThresholdStore, query: ScheduledBrokerThresholdQuery): MutableList<ScheduledBrokerThreshold> {
    return query.apply(store.list())
}
