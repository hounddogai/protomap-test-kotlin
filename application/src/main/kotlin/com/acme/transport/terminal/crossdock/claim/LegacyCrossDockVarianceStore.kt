package com.acme.transport.terminal.crossdock.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy cross dock variances by code. */
interface LegacyCrossDockVarianceStore {
    fun find(code: String): LegacyCrossDockVariance?
    fun save(item: LegacyCrossDockVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyCrossDockVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyCrossDockVarianceStore(
    var items: MutableMap<String, LegacyCrossDockVariance>,
    var order: MutableList<String>,
) : LegacyCrossDockVarianceStore {
    override fun find(code: String): LegacyCrossDockVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyCrossDockVariance) {
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

    override fun list(): MutableList<LegacyCrossDockVariance> {
        val out = mutableListOf<LegacyCrossDockVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyCrossDockVarianceStore(): LegacyCrossDockVarianceStore {
    return MemoryLegacyCrossDockVarianceStore(
        mutableMapOf<String, LegacyCrossDockVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyCrossDockVarianceStore(
    var inner: LegacyCrossDockVarianceStore,
    var cache: TtlCache<LegacyCrossDockVariance>,
    var hits: Int,
    var misses: Int,
) : LegacyCrossDockVarianceStore {
    override fun find(code: String): LegacyCrossDockVariance? {
        val hit = (cache.get(code) as LegacyCrossDockVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyCrossDockVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyCrossDockVariance> = inner.list()
}

fun newCachedLegacyCrossDockVarianceStore(inner: LegacyCrossDockVarianceStore, ttl: Int, clock: Clock): LegacyCrossDockVarianceStore {
    return CachedLegacyCrossDockVarianceStore(
        inner = inner,
        cache = newTtlCache<LegacyCrossDockVariance>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy cross dock variances. Empty fields match all. */
data class LegacyCrossDockVarianceQuery(
    var region: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyCrossDockVariance): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<LegacyCrossDockVariance>): MutableList<LegacyCrossDockVariance> {
        val matched = filter(items) { candidate: LegacyCrossDockVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyCrossDockVariances(store: LegacyCrossDockVarianceStore, query: LegacyCrossDockVarianceQuery): MutableList<LegacyCrossDockVariance> {
    return query.apply(store.list())
}
