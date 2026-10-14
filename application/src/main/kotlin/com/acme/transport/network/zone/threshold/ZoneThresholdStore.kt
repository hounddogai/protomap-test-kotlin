package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone thresholds by code. */
interface ZoneThresholdStore {
    fun find(code: String): ZoneThreshold?
    fun save(item: ZoneThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneThresholdStore(
    var items: MutableMap<String, ZoneThreshold>,
    var order: MutableList<String>,
) : ZoneThresholdStore {
    override fun find(code: String): ZoneThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneThreshold) {
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

    override fun list(): MutableList<ZoneThreshold> {
        val out = mutableListOf<ZoneThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneThresholdStore(): ZoneThresholdStore {
    return MemoryZoneThresholdStore(mutableMapOf<String, ZoneThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneThresholdStore(
    var inner: ZoneThresholdStore,
    var cache: TtlCache<ZoneThreshold>,
    var hits: Int,
    var misses: Int,
) : ZoneThresholdStore {
    override fun find(code: String): ZoneThreshold? {
        val hit = (cache.get(code) as ZoneThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneThreshold> = inner.list()
}

fun newCachedZoneThresholdStore(inner: ZoneThresholdStore, ttl: Int, clock: Clock): ZoneThresholdStore {
    return CachedZoneThresholdStore(
        inner = inner,
        cache = newTtlCache<ZoneThreshold>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone thresholds. Empty fields match all. */
data class ZoneThresholdQuery(
    var label: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneThreshold): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<ZoneThreshold>): MutableList<ZoneThreshold> {
        val matched = filter(items) { candidate: ZoneThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneThresholds(store: ZoneThresholdStore, query: ZoneThresholdQuery): MutableList<ZoneThreshold> {
    return query.apply(store.list())
}
