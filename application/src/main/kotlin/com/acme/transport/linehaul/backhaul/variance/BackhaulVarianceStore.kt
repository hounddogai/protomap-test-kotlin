package com.acme.transport.linehaul.backhaul.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul variances by code. */
interface BackhaulVarianceStore {
    fun find(code: String): BackhaulVariance?
    fun save(item: BackhaulVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulVarianceStore(
    var items: MutableMap<String, BackhaulVariance>,
    var order: MutableList<String>,
) : BackhaulVarianceStore {
    override fun find(code: String): BackhaulVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulVariance) {
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

    override fun list(): MutableList<BackhaulVariance> {
        val out = mutableListOf<BackhaulVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulVarianceStore(): BackhaulVarianceStore {
    return MemoryBackhaulVarianceStore(mutableMapOf<String, BackhaulVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulVarianceStore(
    var inner: BackhaulVarianceStore,
    var cache: TtlCache<BackhaulVariance>,
    var hits: Int,
    var misses: Int,
) : BackhaulVarianceStore {
    override fun find(code: String): BackhaulVariance? {
        val hit = (cache.get(code) as BackhaulVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulVariance> = inner.list()
}

fun newCachedBackhaulVarianceStore(inner: BackhaulVarianceStore, ttl: Int, clock: Clock): BackhaulVarianceStore {
    return CachedBackhaulVarianceStore(
        inner = inner,
        cache = newTtlCache<BackhaulVariance>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul variances. Empty fields match all. */
data class BackhaulVarianceQuery(
    var category: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulVariance): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulVariance>): MutableList<BackhaulVariance> {
        val matched = filter(items) { candidate: BackhaulVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulVariances(store: BackhaulVarianceStore, query: BackhaulVarianceQuery): MutableList<BackhaulVariance> {
    return query.apply(store.list())
}
