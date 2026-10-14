package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped backhaul variances by code. */
interface CappedBackhaulVarianceStore {
    fun find(code: String): CappedBackhaulVariance?
    fun save(item: CappedBackhaulVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedBackhaulVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedBackhaulVarianceStore(
    var items: MutableMap<String, CappedBackhaulVariance>,
    var order: MutableList<String>,
) : CappedBackhaulVarianceStore {
    override fun find(code: String): CappedBackhaulVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedBackhaulVariance) {
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

    override fun list(): MutableList<CappedBackhaulVariance> {
        val out = mutableListOf<CappedBackhaulVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedBackhaulVarianceStore(): CappedBackhaulVarianceStore {
    return MemoryCappedBackhaulVarianceStore(
        mutableMapOf<String, CappedBackhaulVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedBackhaulVarianceStore(
    var inner: CappedBackhaulVarianceStore,
    var cache: TtlCache<CappedBackhaulVariance>,
    var hits: Int,
    var misses: Int,
) : CappedBackhaulVarianceStore {
    override fun find(code: String): CappedBackhaulVariance? {
        val hit = (cache.get(code) as CappedBackhaulVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedBackhaulVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedBackhaulVariance> = inner.list()
}

fun newCachedCappedBackhaulVarianceStore(inner: CappedBackhaulVarianceStore, ttl: Int, clock: Clock): CappedBackhaulVarianceStore {
    return CachedCappedBackhaulVarianceStore(
        inner = inner,
        cache = newTtlCache<CappedBackhaulVariance>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped backhaul variances. Empty fields match all. */
data class CappedBackhaulVarianceQuery(
    var title: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedBackhaulVariance): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedBackhaulVariance>): MutableList<CappedBackhaulVariance> {
        val matched = filter(items) { candidate: CappedBackhaulVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedBackhaulVariances(store: CappedBackhaulVarianceStore, query: CappedBackhaulVarianceQuery): MutableList<CappedBackhaulVariance> {
    return query.apply(store.list())
}
