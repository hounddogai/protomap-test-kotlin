package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps tiered camera event rollups by code. */
interface TieredCameraEventRollupStore {
    fun find(code: String): TieredCameraEventRollup?
    fun save(item: TieredCameraEventRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCameraEventRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCameraEventRollupStore(
    var items: MutableMap<String, TieredCameraEventRollup>,
    var order: MutableList<String>,
) : TieredCameraEventRollupStore {
    override fun find(code: String): TieredCameraEventRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCameraEventRollup) {
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

    override fun list(): MutableList<TieredCameraEventRollup> {
        val out = mutableListOf<TieredCameraEventRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCameraEventRollupStore(): TieredCameraEventRollupStore {
    return MemoryTieredCameraEventRollupStore(
        mutableMapOf<String, TieredCameraEventRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCameraEventRollupStore(
    var inner: TieredCameraEventRollupStore,
    var cache: TtlCache<TieredCameraEventRollup>,
    var hits: Int,
    var misses: Int,
) : TieredCameraEventRollupStore {
    override fun find(code: String): TieredCameraEventRollup? {
        val hit = (cache.get(code) as TieredCameraEventRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCameraEventRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCameraEventRollup> = inner.list()
}

fun newCachedTieredCameraEventRollupStore(inner: TieredCameraEventRollupStore, ttl: Int, clock: Clock): TieredCameraEventRollupStore {
    return CachedTieredCameraEventRollupStore(
        inner = inner,
        cache = newTtlCache<TieredCameraEventRollup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered camera event rollups. Empty fields match all. */
data class TieredCameraEventRollupQuery(
    var lane: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCameraEventRollup): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredCameraEventRollup>): MutableList<TieredCameraEventRollup> {
        val matched = filter(items) { candidate: TieredCameraEventRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCameraEventRollups(store: TieredCameraEventRollupStore, query: TieredCameraEventRollupQuery): MutableList<TieredCameraEventRollup> {
    return query.apply(store.list())
}
