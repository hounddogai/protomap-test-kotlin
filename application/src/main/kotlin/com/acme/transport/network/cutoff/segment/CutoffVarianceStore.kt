package com.acme.transport.network.cutoff.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff variances by code. */
interface CutoffVarianceStore {
    fun find(code: String): CutoffVariance?
    fun save(item: CutoffVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffVarianceStore(
    var items: MutableMap<String, CutoffVariance>,
    var order: MutableList<String>,
) : CutoffVarianceStore {
    override fun find(code: String): CutoffVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffVariance) {
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

    override fun list(): MutableList<CutoffVariance> {
        val out = mutableListOf<CutoffVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffVarianceStore(): CutoffVarianceStore {
    return MemoryCutoffVarianceStore(mutableMapOf<String, CutoffVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffVarianceStore(
    var inner: CutoffVarianceStore,
    var cache: TtlCache<CutoffVariance>,
    var hits: Int,
    var misses: Int,
) : CutoffVarianceStore {
    override fun find(code: String): CutoffVariance? {
        val hit = (cache.get(code) as CutoffVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffVariance> = inner.list()
}

fun newCachedCutoffVarianceStore(inner: CutoffVarianceStore, ttl: Int, clock: Clock): CutoffVarianceStore {
    return CachedCutoffVarianceStore(
        inner = inner,
        cache = newTtlCache<CutoffVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff variances. Empty fields match all. */
data class CutoffVarianceQuery(
    var source: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffVariance): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<CutoffVariance>): MutableList<CutoffVariance> {
        val matched = filter(items) { candidate: CutoffVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffVariances(store: CutoffVarianceStore, query: CutoffVarianceQuery): MutableList<CutoffVariance> {
    return query.apply(store.list())
}
