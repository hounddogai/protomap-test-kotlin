package com.acme.transport.terminal.traileryard.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps rolling trailer yard variances by code. */
interface RollingTrailerYardVarianceStore {
    fun find(code: String): RollingTrailerYardVariance?
    fun save(item: RollingTrailerYardVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingTrailerYardVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingTrailerYardVarianceStore(
    var items: MutableMap<String, RollingTrailerYardVariance>,
    var order: MutableList<String>,
) : RollingTrailerYardVarianceStore {
    override fun find(code: String): RollingTrailerYardVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingTrailerYardVariance) {
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

    override fun list(): MutableList<RollingTrailerYardVariance> {
        val out = mutableListOf<RollingTrailerYardVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingTrailerYardVarianceStore(): RollingTrailerYardVarianceStore {
    return MemoryRollingTrailerYardVarianceStore(
        mutableMapOf<String, RollingTrailerYardVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingTrailerYardVarianceStore(
    var inner: RollingTrailerYardVarianceStore,
    var cache: TtlCache<RollingTrailerYardVariance>,
    var hits: Int,
    var misses: Int,
) : RollingTrailerYardVarianceStore {
    override fun find(code: String): RollingTrailerYardVariance? {
        val hit = (cache.get(code) as RollingTrailerYardVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingTrailerYardVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingTrailerYardVariance> = inner.list()
}

fun newCachedRollingTrailerYardVarianceStore(inner: RollingTrailerYardVarianceStore, ttl: Int, clock: Clock): RollingTrailerYardVarianceStore {
    return CachedRollingTrailerYardVarianceStore(
        inner = inner,
        cache = newTtlCache<RollingTrailerYardVariance>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling trailer yard variances. Empty fields match all. */
data class RollingTrailerYardVarianceQuery(
    var country: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingTrailerYardVariance): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingTrailerYardVariance>): MutableList<RollingTrailerYardVariance> {
        val matched = filter(items) { candidate: RollingTrailerYardVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingTrailerYardVariances(store: RollingTrailerYardVarianceStore, query: RollingTrailerYardVarianceQuery): MutableList<RollingTrailerYardVariance> {
    return query.apply(store.list())
}
