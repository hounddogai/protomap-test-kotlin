package com.acme.transport.linehaul.laneplan.planthreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tiered lane plan thresholds by code. */
interface TieredLanePlanThresholdStore {
    fun find(code: String): TieredLanePlanThreshold?
    fun save(item: TieredLanePlanThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredLanePlanThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredLanePlanThresholdStore(
    var items: MutableMap<String, TieredLanePlanThreshold>,
    var order: MutableList<String>,
) : TieredLanePlanThresholdStore {
    override fun find(code: String): TieredLanePlanThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredLanePlanThreshold) {
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

    override fun list(): MutableList<TieredLanePlanThreshold> {
        val out = mutableListOf<TieredLanePlanThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredLanePlanThresholdStore(): TieredLanePlanThresholdStore {
    return MemoryTieredLanePlanThresholdStore(
        mutableMapOf<String, TieredLanePlanThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredLanePlanThresholdStore(
    var inner: TieredLanePlanThresholdStore,
    var cache: TtlCache<TieredLanePlanThreshold>,
    var hits: Int,
    var misses: Int,
) : TieredLanePlanThresholdStore {
    override fun find(code: String): TieredLanePlanThreshold? {
        val hit = (cache.get(code) as TieredLanePlanThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredLanePlanThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredLanePlanThreshold> = inner.list()
}

fun newCachedTieredLanePlanThresholdStore(inner: TieredLanePlanThresholdStore, ttl: Int, clock: Clock): TieredLanePlanThresholdStore {
    return CachedTieredLanePlanThresholdStore(
        inner = inner,
        cache = newTtlCache<TieredLanePlanThreshold>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered lane plan thresholds. Empty fields match all. */
data class TieredLanePlanThresholdQuery(
    var name: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredLanePlanThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredLanePlanThreshold>): MutableList<TieredLanePlanThreshold> {
        val matched = filter(items) { candidate: TieredLanePlanThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredLanePlanThresholds(store: TieredLanePlanThresholdStore, query: TieredLanePlanThresholdQuery): MutableList<TieredLanePlanThreshold> {
    return query.apply(store.list())
}
