package com.acme.transport.linehaul.trailer.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer thresholds by code. */
interface TrailerThresholdStore {
    fun find(code: String): TrailerThreshold?
    fun save(item: TrailerThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerThresholdStore(
    var items: MutableMap<String, TrailerThreshold>,
    var order: MutableList<String>,
) : TrailerThresholdStore {
    override fun find(code: String): TrailerThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerThreshold) {
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

    override fun list(): MutableList<TrailerThreshold> {
        val out = mutableListOf<TrailerThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerThresholdStore(): TrailerThresholdStore {
    return MemoryTrailerThresholdStore(mutableMapOf<String, TrailerThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerThresholdStore(
    var inner: TrailerThresholdStore,
    var cache: TtlCache<TrailerThreshold>,
    var hits: Int,
    var misses: Int,
) : TrailerThresholdStore {
    override fun find(code: String): TrailerThreshold? {
        val hit = (cache.get(code) as TrailerThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerThreshold> = inner.list()
}

fun newCachedTrailerThresholdStore(inner: TrailerThresholdStore, ttl: Int, clock: Clock): TrailerThresholdStore {
    return CachedTrailerThresholdStore(
        inner = inner,
        cache = newTtlCache<TrailerThreshold>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer thresholds. Empty fields match all. */
data class TrailerThresholdQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerThreshold>): MutableList<TrailerThreshold> {
        val matched = filter(items) { candidate: TrailerThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerThresholds(store: TrailerThresholdStore, query: TrailerThresholdQuery): MutableList<TrailerThreshold> {
    return query.apply(store.list())
}
