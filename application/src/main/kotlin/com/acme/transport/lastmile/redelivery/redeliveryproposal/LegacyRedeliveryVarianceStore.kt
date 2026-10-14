package com.acme.transport.lastmile.redelivery.redeliveryproposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps legacy redelivery variances by code. */
interface LegacyRedeliveryVarianceStore {
    fun find(code: String): LegacyRedeliveryVariance?
    fun save(item: LegacyRedeliveryVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyRedeliveryVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyRedeliveryVarianceStore(
    var items: MutableMap<String, LegacyRedeliveryVariance>,
    var order: MutableList<String>,
) : LegacyRedeliveryVarianceStore {
    override fun find(code: String): LegacyRedeliveryVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyRedeliveryVariance) {
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

    override fun list(): MutableList<LegacyRedeliveryVariance> {
        val out = mutableListOf<LegacyRedeliveryVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyRedeliveryVarianceStore(): LegacyRedeliveryVarianceStore {
    return MemoryLegacyRedeliveryVarianceStore(
        mutableMapOf<String, LegacyRedeliveryVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyRedeliveryVarianceStore(
    var inner: LegacyRedeliveryVarianceStore,
    var cache: TtlCache<LegacyRedeliveryVariance>,
    var hits: Int,
    var misses: Int,
) : LegacyRedeliveryVarianceStore {
    override fun find(code: String): LegacyRedeliveryVariance? {
        val hit = (cache.get(code) as LegacyRedeliveryVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyRedeliveryVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyRedeliveryVariance> = inner.list()
}

fun newCachedLegacyRedeliveryVarianceStore(inner: LegacyRedeliveryVarianceStore, ttl: Int, clock: Clock): LegacyRedeliveryVarianceStore {
    return CachedLegacyRedeliveryVarianceStore(
        inner = inner,
        cache = newTtlCache<LegacyRedeliveryVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy redelivery variances. Empty fields match all. */
data class LegacyRedeliveryVarianceQuery(
    var name: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyRedeliveryVariance): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyRedeliveryVariance>): MutableList<LegacyRedeliveryVariance> {
        val matched = filter(items) { candidate: LegacyRedeliveryVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyRedeliveryVariances(store: LegacyRedeliveryVarianceStore, query: LegacyRedeliveryVarianceQuery): MutableList<LegacyRedeliveryVariance> {
    return query.apply(store.list())
}
