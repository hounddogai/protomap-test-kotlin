package com.acme.transport.network.cutoff.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff thresholds by code. */
interface CutoffThresholdStore {
    fun find(code: String): CutoffThreshold?
    fun save(item: CutoffThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffThresholdStore(
    var items: MutableMap<String, CutoffThreshold>,
    var order: MutableList<String>,
) : CutoffThresholdStore {
    override fun find(code: String): CutoffThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffThreshold) {
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

    override fun list(): MutableList<CutoffThreshold> {
        val out = mutableListOf<CutoffThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffThresholdStore(): CutoffThresholdStore {
    return MemoryCutoffThresholdStore(mutableMapOf<String, CutoffThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffThresholdStore(
    var inner: CutoffThresholdStore,
    var cache: TtlCache<CutoffThreshold>,
    var hits: Int,
    var misses: Int,
) : CutoffThresholdStore {
    override fun find(code: String): CutoffThreshold? {
        val hit = (cache.get(code) as CutoffThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffThreshold> = inner.list()
}

fun newCachedCutoffThresholdStore(inner: CutoffThresholdStore, ttl: Int, clock: Clock): CutoffThresholdStore {
    return CachedCutoffThresholdStore(
        inner = inner,
        cache = newTtlCache<CutoffThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff thresholds. Empty fields match all. */
data class CutoffThresholdQuery(
    var title: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffThreshold): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffThreshold>): MutableList<CutoffThreshold> {
        val matched = filter(items) { candidate: CutoffThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffThresholds(store: CutoffThresholdStore, query: CutoffThresholdQuery): MutableList<CutoffThreshold> {
    return query.apply(store.list())
}
