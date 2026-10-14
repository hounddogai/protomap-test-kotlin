package com.acme.transport.lastmile.locker.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker thresholds by code. */
interface LockerThresholdStore {
    fun find(code: String): LockerThreshold?
    fun save(item: LockerThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerThresholdStore(
    var items: MutableMap<String, LockerThreshold>,
    var order: MutableList<String>,
) : LockerThresholdStore {
    override fun find(code: String): LockerThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerThreshold) {
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

    override fun list(): MutableList<LockerThreshold> {
        val out = mutableListOf<LockerThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerThresholdStore(): LockerThresholdStore {
    return MemoryLockerThresholdStore(mutableMapOf<String, LockerThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerThresholdStore(
    var inner: LockerThresholdStore,
    var cache: TtlCache<LockerThreshold>,
    var hits: Int,
    var misses: Int,
) : LockerThresholdStore {
    override fun find(code: String): LockerThreshold? {
        val hit = (cache.get(code) as LockerThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerThreshold> = inner.list()
}

fun newCachedLockerThresholdStore(inner: LockerThresholdStore, ttl: Int, clock: Clock): LockerThresholdStore {
    return CachedLockerThresholdStore(
        inner = inner,
        cache = newTtlCache<LockerThreshold>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker thresholds. Empty fields match all. */
data class LockerThresholdQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<LockerThreshold>): MutableList<LockerThreshold> {
        val matched = filter(items) { candidate: LockerThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerThresholds(store: LockerThresholdStore, query: LockerThresholdQuery): MutableList<LockerThreshold> {
    return query.apply(store.list())
}
