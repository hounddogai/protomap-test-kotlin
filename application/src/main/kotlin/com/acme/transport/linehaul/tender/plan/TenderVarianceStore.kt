package com.acme.transport.linehaul.tender.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender variances by code. */
interface TenderVarianceStore {
    fun find(code: String): TenderVariance?
    fun save(item: TenderVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderVarianceStore(
    var items: MutableMap<String, TenderVariance>,
    var order: MutableList<String>,
) : TenderVarianceStore {
    override fun find(code: String): TenderVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderVariance) {
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

    override fun list(): MutableList<TenderVariance> {
        val out = mutableListOf<TenderVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderVarianceStore(): TenderVarianceStore {
    return MemoryTenderVarianceStore(mutableMapOf<String, TenderVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderVarianceStore(
    var inner: TenderVarianceStore,
    var cache: TtlCache<TenderVariance>,
    var hits: Int,
    var misses: Int,
) : TenderVarianceStore {
    override fun find(code: String): TenderVariance? {
        val hit = (cache.get(code) as TenderVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderVariance> = inner.list()
}

fun newCachedTenderVarianceStore(inner: TenderVarianceStore, ttl: Int, clock: Clock): TenderVarianceStore {
    return CachedTenderVarianceStore(
        inner = inner,
        cache = newTtlCache<TenderVariance>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender variances. Empty fields match all. */
data class TenderVarianceQuery(
    var source: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderVariance): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<TenderVariance>): MutableList<TenderVariance> {
        val matched = filter(items) { candidate: TenderVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderVariances(store: TenderVarianceStore, query: TenderVarianceQuery): MutableList<TenderVariance> {
    return query.apply(store.list())
}
