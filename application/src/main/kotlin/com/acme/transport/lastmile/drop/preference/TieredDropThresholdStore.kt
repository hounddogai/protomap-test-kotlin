package com.acme.transport.lastmile.drop.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps tiered drop thresholds by code. */
interface TieredDropThresholdStore {
    fun find(code: String): TieredDropThreshold?
    fun save(item: TieredDropThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDropThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDropThresholdStore(
    var items: MutableMap<String, TieredDropThreshold>,
    var order: MutableList<String>,
) : TieredDropThresholdStore {
    override fun find(code: String): TieredDropThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDropThreshold) {
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

    override fun list(): MutableList<TieredDropThreshold> {
        val out = mutableListOf<TieredDropThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDropThresholdStore(): TieredDropThresholdStore {
    return MemoryTieredDropThresholdStore(mutableMapOf<String, TieredDropThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDropThresholdStore(
    var inner: TieredDropThresholdStore,
    var cache: TtlCache<TieredDropThreshold>,
    var hits: Int,
    var misses: Int,
) : TieredDropThresholdStore {
    override fun find(code: String): TieredDropThreshold? {
        val hit = (cache.get(code) as TieredDropThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDropThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDropThreshold> = inner.list()
}

fun newCachedTieredDropThresholdStore(inner: TieredDropThresholdStore, ttl: Int, clock: Clock): TieredDropThresholdStore {
    return CachedTieredDropThresholdStore(
        inner = inner,
        cache = newTtlCache<TieredDropThreshold>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered drop thresholds. Empty fields match all. */
data class TieredDropThresholdQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDropThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredDropThreshold>): MutableList<TieredDropThreshold> {
        val matched = filter(items) { candidate: TieredDropThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDropThresholds(store: TieredDropThresholdStore, query: TieredDropThresholdQuery): MutableList<TieredDropThreshold> {
    return query.apply(store.list())
}
